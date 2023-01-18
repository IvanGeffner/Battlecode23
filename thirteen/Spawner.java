package thirteen;

import battlecode.common.*;

public class Spawner {
    static RobotController rc;
    boolean danger;
    static int round;
    static RobotType constructionType;
    static BuildRobotLoc[] robotLocs;
    static MapLocation enemyLoc;

    static MapLocation target;

    Spawner() throws GameActionException {
        this.rc = Robot.rc;
        computeLowVision();
        fillSpiral();
        buildRobotLocs();
    }

    int constructRobotGreedy(RobotType t, MapLocation target){
        try {
            constructionType = t;
            this.target = target;
            round = rc.getRoundNum();
            checkDanger();
            BuildRobotLoc bestBRL = null;
            for (int i = robotLocs.length; i-- > 0; ) {
                robotLocs[i].update();
            }
            for (RobotInfo r : enemies){
                if (Clock.getBytecodeNum() > 7000) break;
                if (r.getType() != RobotType.LAUNCHER) continue;
                for (int i = robotLocs.length; i-- > 0; ) {
                    enemyLoc = r.getLocation();
                    robotLocs[i].updateEnemyLauncher();
                }
            }

            for (int i = robotLocs.length; i-- > 0; ) {
                if (robotLocs[i].isBetterThan(bestBRL)) bestBRL = robotLocs[i];
            }
            if (bestBRL != null){
                if (rc.canBuildRobot(t, bestBRL.loc)){
                    rc.buildRobot(t, bestBRL.loc);
                    RobotInfo r = rc.senseRobotAtLocation(bestBRL.loc);
                    if (r != null) return r.getID();
                }
                return -1;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    class BuildRobotLoc {

        MapLocation loc;
        int distToTarget;
        int minDistToLauncher;
        boolean canBuild;

        BuildRobotLoc(MapLocation loc){
            this.loc = loc;
        }

        void update(){
            minDistToLauncher = 10000;
            canBuild = rc.canBuildRobot(constructionType, loc);
            distToTarget = this.loc.distanceSquaredTo(target);
        }

        void updateEnemyLauncher(){
            int d = enemyLoc.distanceSquaredTo(this.loc);
            if (d < minDistToLauncher) minDistToLauncher = d;
        }

        boolean isDangerous(){
            return minDistToLauncher <= RobotType.LAUNCHER.actionRadiusSquared;
        }

        boolean isBetterThan(BuildRobotLoc brl){
            if (!canBuild) return false;
            if (brl == null || !brl.canBuild) return true;

            if (!isDangerous() && brl.isDangerous()) return true;
            if (isDangerous() && !brl.isDangerous()) return false;

            if (isDangerous()){
                return minDistToLauncher > brl.minDistToLauncher;
            }
            return distToTarget < brl.distToTarget;
        }

    }

    static Direction[] spiral;
    int myVision;
    RobotInfo[] enemies;

    boolean checkDanger() throws GameActionException {
        enemies = rc.senseNearbyRobots(myVision, rc.getTeam().opponent());
        for (RobotInfo r : enemies){
            if (r.getType() == RobotType.LAUNCHER){
                danger = true;
                return true;
            }
        }
        danger = false;
        return false;
    }

    void computeLowVision() throws GameActionException {
        MapInfo myMapInfo = rc.senseMapInfo(rc.getLocation()); //TODO: change this
        if (myMapInfo.hasCloud()) myVision = GameConstants.CLOUD_VISION_RADIUS_SQUARED;
        else myVision = rc.getType().visionRadiusSquared;
    }

    void fillSpiral(){
        if (myVision == GameConstants.CLOUD_VISION_RADIUS_SQUARED){
            spiral = new Direction[]{Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTHEAST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.WEST, Direction.CENTER};
            return;
        }
        spiral = new Direction[]{Direction.NORTHWEST, Direction.WEST, Direction.NORTH, Direction.NORTHWEST, Direction.NORTHEAST, Direction.NORTH, Direction.EAST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.EAST, Direction.SOUTH, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.SOUTH, Direction.WEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTHEAST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.WEST, Direction.CENTER};
    }

    void buildRobotLocs(){
        MapLocation loc = rc.getLocation();
        robotLocs = new BuildRobotLoc[spiral.length];
        for (int i = robotLocs.length; i-- > 0; ){
            loc = loc.add(spiral[i]);
            robotLocs[i] = new BuildRobotLoc(loc);
        }
    }

}
