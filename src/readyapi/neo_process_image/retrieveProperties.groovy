package readyapi.neo_process_image

import groovy.json.JsonSlurper

try {
    retrieveProperties()
    retrieveTestChoices()
} catch (Exception ex) {
    log.error(ex.getMessage())
    testRunner.fail("Failed to prepare data")
}

void retrieveProperties() {
    String priority = testRunner.testCase.getPropertyValue("priority")?.trim()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("priority", priority ? priority : "42")

    String processId = testRunner.testCase.getPropertyValue("processId")?.trim()?.toLowerCase()
    processId = processId ? processId : "com.airbus.massive.ortho.v3"
    testRunner.testCase.testSteps["Properties"].setPropertyValue("processId", processId)

    String catalogId = context.expand('${#TestCase#catalogId}')
    String workspace = context.expand('${#TestCase#workspace}')
    String executionOrder = testRunner.testCase.getPropertyValue("executionOrder")?.trim()
    if (executionOrder) {
        testRunner.testCase.testSteps["Properties"].setPropertyValue("geometry", null)
        executionOrder = executionOrder.replaceFirst("\"catalogId\"\\s*:\\s*\"[^\\s\"]*\"", "\"catalogId\":\"${catalogId}\"")
                .replaceFirst("\"workspace\"\\s*:\\s*\"[^\\s\"]*\"", "\"workspace\":\"${workspace}\"")
    } else {
        String geometry = context.expand('${#TestCase#geometry}')?.trim()
        if (!checkIsValidGeometryPayload(geometry)) {
            String geometryFormat = geometry ==~ "(tiny)|(very-tiny)" ? geometry : 'tiny'
            String catalogGeometry = context.expand('${Properties#catalogGeometry}')
            geometry = computeGeometryPayload(geometryFormat, catalogGeometry)
        }
        log.info(geometry)
        testRunner.testCase.testSteps["Properties"].setPropertyValue("geometry", geometry)
        executionOrder = computeExecutionOrder(processId, catalogId, workspace, geometry)
    }
    testRunner.testCase.testSteps["Properties"].setPropertyValue("executionOrder", executionOrder)

    String userRole = context.expand('${#TestCase#userRole}')?.trim()?.toLowerCase()
    userRole = userRole ==~ "admin" ? 'admin' : 'impersonate'
    testRunner.testCase.testSteps["Properties"].setPropertyValue("userRole", userRole)
    if (userRole == "admin") {
        testRunner.testCase.testSteps["Properties"].setPropertyValue("forwardedUser", "")
        testRunner.gotoStepByName("Start orthorectification order")
    }
}

void retrieveTestChoices() {
    String checkStreaming = testRunner.testCase.getPropertyValue("checkStreaming")?.trim()?.toLowerCase()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("checkStreaming", checkStreaming ==~ "(yes)|(true)" ? 'true' : 'false')

    String checkQuicklookThumbnail = testRunner.testCase.getPropertyValue("checkQuicklookThumbnail")?.trim()?.toLowerCase()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("checkQuicklookThumbnail", checkQuicklookThumbnail ==~ "(yes)|(true)" ? 'true' : 'false')

    String correlationID = testRunner.testCase.getPropertyValue("correlationID")?.trim()
    testRunner.testCase.testSteps["Properties"].setPropertyValue("checkCorrelationID", correlationID ? 'true' : 'false')
}

Boolean checkIsValidGeometryPayload(String geometry) {
    try {
        new JsonSlurper().parseText(geometry)
        return true
    } catch (Exception e) {
        return false
    }
}

String computeGeometryPayload(String geometryFormat, String catalogGeometry) throws Exception {
    BigDecimal lengthSquare
    BigDecimal halfLength

    switch (geometryFormat) {
        case "tiny":
            log.info("geometry will be reduced to a small square at the center of the image")
            lengthSquare = 0.005
            halfLength = lengthSquare / 2
            break
        case "very-tiny":
            log.info("geometry will be reduced to a very-small square at the center of the image")
            lengthSquare = 0.0001
            halfLength = lengthSquare / 2
            break
        default:
            log.error("Unexpected geometry format: [$geometryFormat]")
            throw new Exception("Unexpected geometry format")
    }

    geometry = new JsonSlurper().parseText(catalogGeometry)
    ArrayList centroid = getCentroid(geometry)
    geometry["coordinates"][0] = [
            [centroid[0] - halfLength, centroid[1] - halfLength],
            [centroid[0] + halfLength, centroid[1] - halfLength],
            [centroid[0] + halfLength, centroid[1] + halfLength],
            [centroid[0] - halfLength, centroid[1] + halfLength],
            [centroid[0] - halfLength, centroid[1] - halfLength],
    ]
    return groovy.json.JsonOutput.toJson(geometry)
}

ArrayList getCentroid(geometry) {
    BigDecimal area = getArea(geometry)
    BigDecimal centroidX = 0
    BigDecimal centroidY = 0
    ArrayList points = geometry["coordinates"][0]
    Integer size = points.size() - 1
    for (def i = 0; i < size; i++) {
        centroidX += (points[i][0] + points[i + 1][0]) * (points[i][0] * points[i + 1][1] - points[i + 1][0] * points[i][1])
        centroidY += (points[i][1] + points[i + 1][1]) * (points[i][0] * points[i + 1][1] - points[i + 1][0] * points[i][1])
    }
    return [centroidX / (6 * area), centroidY / (6 * area)]
}

BigDecimal getArea(geometry) {
    BigDecimal area = 0
    ArrayList points = geometry["coordinates"][0]
    Integer size = points.size() - 1
    for (def i = 0; i < size; i++) {
        area += points[i][0] * points[i + 1][1] - points[i + 1][0] * points[i][1]
    }
    return area / 2
}

String computeExecutionOrder(String processId, String catalogId, String workspace, String geometry) {
    String executionOrder
    switch (processId) {
        case "com.airbus.massive.ortho":
            executionOrder = """{
    "catalogId": "${catalogId}",
    "workspace": "${workspace}",
    "AOI": {
        "type": "Feature",
        "properties": {
            "crsCode": "urn:ogc:def:crs:EPSG::4326"
        },
        "geometry":${geometry}
    },
    "DEMType": "MIXEDDEM",
    "tilingSize": 2048,
    "outCRS": "urn:ogc:def:crs:EPSG::4326",
    "outputStep": {
        "x": 0.00000462962963,
        "y": 0.00000462962963
    },
    "lengthSegmentMax": 0.01,
    "optimize": "performance",
    "pansharpened": false,
    "greyWeights": {
        "B0": 0.251,
        "B1": 0.313,
        "B2": 0.367,
        "B3": 0.057
    },
    "footprintMargin": 0,
    "resamplingMethod": "BICUBIC",
    "radiometricProcessing": "REFLECTANCE"
}"""
            break
        case "com.airbus.massive.ortho.v2":
            executionOrder = """{
    "catalogId": "${catalogId}",
    "workspace": "${workspace}",
    "AOI": {
        "type": "Feature",
        "properties": {
            "crsCode": "urn:ogc:def:crs:EPSG::4326"
        },
        "geometry":${geometry}
    },
    "commercialReference": "ORTHO PMS DISPLAY JPEG2000 3857 NE",
    "customerReference": "ORTHO PMS DISPLAY JPEG2000 3857 NE",
    "comment": "com.airbus.massive.ortho.v2",
    "outputStep": {
        "x": 0.5,
        "y": 0.5
    },
    "DEMType": "MIXEDDEM",
    "tilingSize": 2048,
    "outCRS": "urn:ogc:def:crs:EPSG::3857",
    "lengthSegmentMax": 0.01,
    "optimize": "performance",
    "productType": "PMS",
    "greyWeights": {
        "B0": 0.148,
        "B1": 0.334,
        "B2": 0.487,
        "B3": 0.051
    },
    "footprintMargin": 0,
    "radiometricProcessing": "DISPLAY",
    "resamplingMethod": "BICUBIC"
}"""
            break
        case "com.airbus.massive.ortho.v3":
            executionOrder = """{
    "catalogId": "${catalogId}",
    "workspace": "${workspace}",
    "AOI": {
        "type": "Feature",
        "properties": {
            "crsCode": "urn:ogc:def:crs:EPSG::4326"
        },
        "geometry":${geometry}
    },
    "outputStep": {
        "x": 0.5,
        "y": 0.5
    },
    "orderIdentification": {
        "commercialReference": "ORTHO PMS DISPLAY JPEG2000 3857 NE",
        "customerReference": "ORTHO PMS DISPLAY JPEG2000 3857 NE",
        "comment": "com.airbus.massive.ortho.v3"
    },
    "DEMType": "MIXEDDEM",
    "tilingSize": 2048,
    "outputSRS": {
        "type": "urn",
        "value": "urn:ogc:def:crs:EPSG::3857"
    },
    "lengthSegmentMax": 0.01,
    "optimize": "performance",
    "productType": "PMS",
    "greyWeights": {
        "B0": 0.148,
        "B1": 0.334,
        "B2": 0.487,
        "B3": 0.051
    },
    "footprintMargin": 0,
    "radiometricProcessing": "DISPLAY",
    "resamplingMethod": "BICUBIC",
    "resamplingPMS": "BICUBIC",
    "resamplingOrtho": "BICUBIC"
}"""
            break
        default:
            log.error("Unexpected processId format: [$processId]")
            throw new Exception("Unexpected processId")
    }

    return executionOrder
}