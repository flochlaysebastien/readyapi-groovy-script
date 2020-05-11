package readyapi.geofencing_counterclockwise_privileges

import groovy.json.*

def jsonSlurper = new JsonSlurper()
def staticCoordinates = jsonSlurper.parseText(testRunner.testCase.getTestStepByName("Properties").getPropertyValue("geometry"))
def coordinates = jsonSlurper.parseText(testRunner.testCase.getTestStepByName("Properties").getPropertyValue("geometry"))
def coordinatesCount = coordinates.size();

def isAM = false;
for (int i = 0; i < coordinatesCount - 1; i++) {
    def distance = Math.abs(coordinates[i][0] - coordinates[i + 1][0]);
    if (distance >= 180) {
        isAM = true;
        break;
    }
}

if (!isAM) {
    int maxXPointPosition = 0;
    for (int i = 1; i < coordinatesCount - 1; ++i) {
        if (staticCoordinates[maxXPointPosition][0] < staticCoordinates[i][0]) {
            maxXPointPosition = i;
        }
    }

    ArrayList maxXPoint = staticCoordinates[maxXPointPosition];
    ArrayList maxXPreviousPoint = maxXPointPosition > 0 ? staticCoordinates[maxXPointPosition - 1] : staticCoordinates[coordinatesCount - 2];
    ArrayList maxXNextPoint = staticCoordinates[maxXPointPosition + 1];

    double z = (maxXPoint[0] - maxXPreviousPoint[0]) * (maxXNextPoint[1] - maxXPoint[1]) -
            (maxXPoint[1] - maxXPreviousPoint[1]) * (maxXNextPoint[0] - maxXPoint[0]);

    if (z < 0) {
        testRunner.testCase.getTestStepByName("Properties").setPropertyValue("geometryClockwise", coordinates.toString())
    } else {
        testRunner.testCase.getTestStepByName("Properties").setPropertyValue("geometryCounterclockwise", coordinates.toString())
    }

    java.util.Collections.reverse(coordinates)

    if (z >= 0) {
        testRunner.testCase.getTestStepByName("Properties").setPropertyValue("geometryClockwise", coordinates.toString())
    } else {
        testRunner.testCase.getTestStepByName("Properties").setPropertyValue("geometryCounterclockwise", coordinates.toString())
    }
}