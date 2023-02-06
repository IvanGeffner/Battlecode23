package fortytwo;

import battlecode.common.*;

public class Spawner {
    static RobotController rc;
    static int round;
    static RobotType constructionType;
    static BuildRobotLoc[] robotLocs;
    static MapLocation enemyLoc;

    static MapLocation target;

    boolean danger = false;
    int ans = 0;

    int maxAmount = 1;

    Spawner() throws GameActionException {
        this.rc = Robot.rc;
        buildRobotLocs();
    }

    int constructRobotGreedy(RobotType t, MapLocation target, int maxAmount){
        try {
            constructionType = t;
            this.target = target;
            this.maxAmount = maxAmount;

            checkDanger();
            round = rc.getRoundNum();

            ans = 0;

            if (danger) {
                constructDanger(t);
            } else{
                greedyToTarget(t, target);
            }
        } catch (GameActionException e){
            e.printStackTrace();
        }
        return ans;
    }

    boolean first;

    void constructDanger(RobotType t) throws GameActionException {
        first = true;
        for (RobotInfo r : enemies) {
            if (Clock.getBytecodeNum() > 7000) break;
            if (r.getType() != RobotType.LAUNCHER) continue;
            enemyLoc = r.getLocation();
            for (int i = robotLocs.length; i-- > 0; ) {
                robotLocs[i].updateEnemyLauncher();
            }
            first = false;
        }
        for (int i = robotLocs.length; i-- > 0; ){
            if (first || robotLocs[i].minDistToLauncher > 26){
                if (rc.canBuildRobot(t, robotLocs[i].loc)){
                    rc.buildRobot(t, robotLocs[i].loc);
                    ++ans;
                    /*if (rc.canSenseLocation(robotLocs[i].loc)) {
                        RobotInfo r = rc.senseRobotAtLocation(robotLocs[i].loc);
                        if (r != null) ans = r.getID();
                    }*/
                }
            }
        }
    }

    BuildRobotLoc bestLoc1, bestLoc2, bestLoc3, bestLoc4;
    int bestDist1, bestDist2, bestDist3, bestDist4;

    void greedyToTarget(RobotType t, MapLocation target) throws GameActionException{
        bestLoc1 = null; bestLoc2 = null; bestLoc3 = null; bestLoc4 = null;
        for (int i = robotLocs.length; i-- > 0; ){
            if (!rc.canBuildRobot(t, robotLocs[i].loc)) continue;
            int d = target.distanceSquaredTo(robotLocs[i].loc);
            if (bestLoc1 == null || d < bestDist1){
                bestLoc4 = bestLoc3;
                bestDist4 = bestDist3;
                bestLoc3 = bestLoc2;
                bestDist3 = bestDist2;
                bestLoc2 = bestLoc1;
                bestDist2 = bestDist1;
                bestLoc1 = robotLocs[i];
                bestDist1 = d;
            }
            else if (bestLoc2 == null || d < bestDist2){
                bestLoc4 = bestLoc3;
                bestDist4 = bestDist3;
                bestLoc3 = bestLoc2;
                bestDist3 = bestDist2;
                bestLoc2 = robotLocs[i];
                bestDist2 = d;
            }
            else if (bestLoc3 == null || d < bestDist3){
                bestLoc4 = bestLoc3;
                bestDist4 = bestDist3;
                bestLoc3 = robotLocs[i];
                bestDist3 = d;
            }
            else if (bestLoc4 == null || d < bestDist4){
                bestLoc4 = robotLocs[i];
                bestDist4 = d;
            }
        }

        if (bestLoc1 != null && rc.canBuildRobot(t, bestLoc1.loc)){
            rc.buildRobot(t, bestLoc1.loc);
            /*if (rc.canSenseLocation(bestLoc1.loc)){
                RobotInfo r = rc.senseRobotAtLocation(bestLoc1.loc);
                if (r != null) ans = r.getID();
            }*/
            ++ans;
        }
        if (maxAmount <= 1) return;
        if (bestLoc2 != null && rc.canBuildRobot(t, bestLoc2.loc)){
            rc.buildRobot(t, bestLoc2.loc);
            /*if (rc.canSenseLocation(bestLoc2.loc)){
                RobotInfo r = rc.senseRobotAtLocation(bestLoc2.loc);
                if (r != null) ans = r.getID();
            }*/
            ++ans;
        }
        if (maxAmount <= 2) return;
        if (bestLoc3 != null && rc.canBuildRobot(t, bestLoc3.loc)){
            rc.buildRobot(t, bestLoc3.loc);
            /*if (rc.canSenseLocation(bestLoc3.loc)){
                RobotInfo r = rc.senseRobotAtLocation(bestLoc3.loc);
                if (r != null) ans = r.getID();
            }*/
            ++ans;
        }
        if (maxAmount <= 3) return;
        if (bestLoc4 != null && rc.canBuildRobot(t, bestLoc4.loc)){
            rc.buildRobot(t, bestLoc4.loc);
            /*if (rc.canSenseLocation(bestLoc4.loc)){
                RobotInfo r = rc.senseRobotAtLocation(bestLoc4.loc);
                if (r != null) ans = r.getID();
            }*/
            ++ans;
        }
    }

    class BuildRobotLoc {

        MapLocation loc;
        int minDistToLauncher;

        BuildRobotLoc(MapLocation loc){
            this.loc = loc;
        }

        void updateEnemyLauncher(){
            if (first) minDistToLauncher = enemyLoc.distanceSquaredTo(this.loc);
            else {
                int d = enemyLoc.distanceSquaredTo(this.loc);
                if (d < minDistToLauncher) minDistToLauncher = d;
            }
        }
    }

    RobotInfo[] enemies;

    boolean checkDanger() throws GameActionException {
        if (round >= rc.getRoundNum()) return danger;
        enemies = Robot.enemies;
        for (RobotInfo r : enemies){
            if (r.getType() == RobotType.LAUNCHER){
                danger = true;
                return true;
            }
        }
        danger = false;
        return false;
    }

    void buildRobotLocs() throws GameActionException {
        MapLocation[] visibleLocs = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().actionRadiusSquared);
        robotLocs = new BuildRobotLoc[visibleLocs.length];
        for (int i = visibleLocs.length; i-- > 0; ){
            robotLocs[i] = new BuildRobotLoc(visibleLocs[i]);
        }
    }

}
