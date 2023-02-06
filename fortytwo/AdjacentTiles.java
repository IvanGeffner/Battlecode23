package fortytwo;

import battlecode.common.*;

public class AdjacentTiles {

    static DirInfo[] dirDanger;
    static RobotController rc;
    boolean computedSym = false;
    //static Direction[] dirs = Direction.values();
    int[] dangerous;

    int enemyHQIndex = 1;

    Direction[] spiral = new Direction[] {Direction.NORTHWEST, Direction.WEST, Direction.NORTH, Direction.NORTHWEST, Direction.NORTHEAST, Direction.NORTH, Direction.EAST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.EAST, Direction.SOUTH, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.SOUTH, Direction.WEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTHEAST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.SOUTHEAST, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTHEAST, Direction.SOUTHEAST, Direction.WEST, Direction.CENTER};



    static final int HQ_RANGE = RobotType.HEADQUARTERS.actionRadiusSquared;

    MapLocation enemyHQ = null;

    AdjacentTiles() throws GameActionException {
        this.rc = Robot.rc;
        dangerous = new int[(Robot.H * Robot.W + 31)/32];
        dirDanger = new DirInfo[9];
        dirDanger[Direction.CENTER.ordinal()] = new DirInfo(Direction.CENTER);
        dirDanger[Direction.EAST.ordinal()] = new DirInfo(Direction.EAST);
        dirDanger[Direction.NORTHEAST.ordinal()] = new DirInfo(Direction.NORTHEAST);
        dirDanger[Direction.NORTH.ordinal()] = new DirInfo(Direction.NORTH);
        dirDanger[Direction.NORTHWEST.ordinal()] = new DirInfo(Direction.NORTHWEST);
        dirDanger[Direction.WEST.ordinal()] = new DirInfo(Direction.WEST);
        dirDanger[Direction.SOUTHWEST.ordinal()] = new DirInfo(Direction.SOUTHWEST);
        dirDanger[Direction.SOUTH.ordinal()] = new DirInfo(Direction.SOUTH);
        dirDanger[Direction.SOUTHEAST.ordinal()] = new DirInfo(Direction.SOUTHEAST);
    }

    boolean reset() throws GameActionException {
        boolean b = computeSymDanger();
        softReset();
        return b;
    }

    void softReset() throws GameActionException {
        Robot.bytecodeDebug += "At softreset " + Clock.getBytecodeNum() + " ";
        dirDanger[0].update();
        dirDanger[1].update();
        dirDanger[2].update();
        dirDanger[3].update();
        dirDanger[4].update();
        dirDanger[5].update();
        dirDanger[6].update();
        dirDanger[7].update();
        dirDanger[8].update();
        if (!computedSym) computeVisibleHQDanger();
    }

    boolean computeSymDanger() throws GameActionException {
        if (computedSym) return false;
        if (rc.getRoundNum() == Robot.roundBirth) return false;
        if (Robot.otherComm.getSymmetry() == null) return false;
        if (enemyHQIndex >= 4){
            computedSym = true;
            return false;
        }
        int aux;
        int H = rc.getMapHeight();
        MapLocation hqLoc = Robot.hComm.getHQLocation(enemyHQIndex);
        if (hqLoc == null){
            computedSym = true;
            return false;
        }
        enemyHQ = Robot.otherComm.getSymmetric(hqLoc);
        rc.setIndicatorLine(rc.getLocation(), enemyHQ, 200, 200, 200);
        rc.setIndicatorLine(rc.getLocation(), hqLoc, 100, 100, 0);
        for (int j = spiral.length; j-- > 0; ){
            enemyHQ = enemyHQ.add(spiral[j]);
            if (!rc.onTheMap(enemyHQ)) continue;
            aux = enemyHQ.x*H + enemyHQ.y;
            dangerous[aux/32] |= (1 << (aux%32));
        }
        ++enemyHQIndex;
        return true;
    }

    void computeVisibleHQDanger() {
        Robot.bytecodeDebug += "Before VisibleHQ " + Clock.getBytecodeNum() + " ";
        RobotInfo[] robots = Robot.enemies;
        for (RobotInfo r : robots) {
            if (r.getType() != RobotType.HEADQUARTERS) continue;
            enemyHQ = r.getLocation();
            dirDanger[0].updateHQ();
            dirDanger[1].updateHQ();
            dirDanger[2].updateHQ();
            dirDanger[3].updateHQ();
            dirDanger[4].updateHQ();
            dirDanger[5].updateHQ();
            dirDanger[6].updateHQ();
            dirDanger[7].updateHQ();
            dirDanger[8].updateHQ();
        }
        Robot.bytecodeDebug += "After VisibleHQ " + Clock.getBytecodeNum() + " ";
    }


    class DirInfo{
        boolean closeToEnemyHQ = false;
        Direction dir;
        MapLocation endLoc;
        boolean canMove;

        DirInfo(Direction dir) throws GameActionException {
            this.dir = dir;
        }

        void update() throws GameActionException {
            closeToEnemyHQ = true;
            canMove = rc.canMove(dir);
            if (dir == Direction.CENTER) canMove = true;
            endLoc = rc.getLocation();
            endLoc = endLoc.add(dir);
            if (rc.onTheMap(endLoc)) {
                endLoc = endLoc.add(rc.senseMapInfo(endLoc).getCurrentDirection());
                if (rc.canSenseLocation(endLoc)) endLoc = endLoc.add(rc.senseMapInfo(endLoc).getCurrentDirection());
                int aux = endLoc.x* Robot.H+ endLoc.y;
                if ((dangerous[aux/32] & (1 << (aux%32))) != 0) closeToEnemyHQ = true;
                else closeToEnemyHQ = false;
            }
        }

        void updateHQ(){
            if (endLoc.distanceSquaredTo(enemyHQ) <= HQ_RANGE){
                closeToEnemyHQ = true;
                rc.setIndicatorDot(endLoc, 150, 150, 150);
            }
        }
    }
}
