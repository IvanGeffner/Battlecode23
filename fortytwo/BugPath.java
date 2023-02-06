package fortytwo;

import battlecode.common.*;

public class BugPath {

    RobotController rc;

    static int H,W;

    BugPath(){
        this.rc = Robot.rc;
        H = Robot.H; W = Robot.W;
        states = new int[W][];
    }

    int bugPathIndex = 0;

    int stateIndex = 0;

    boolean isReady(){
        return stateIndex >= W;
    }

    void fill(){
        while(stateIndex < W){
            if (Clock.getBytecodesLeft() < 1000) return;
            states[stateIndex++] = new int[H];
        }
    }

    Boolean rotateRight = null; //if I should rotate right or left
    //Boolean rotateRightAux = null;
    MapLocation lastObstacleFound = null; //latest obstacle I've found in my way

    MapLocation lastCurrent = null;
    int minDistToTarget = Constants.INF; //minimum distance I've been to the enemy while going around an obstacle
    MapLocation minLocationToTarget = null;
    MapLocation prevTarget = null; //previous target
    Direction[] dirs = Direction.values();
    //HashSet<Integer> states = new HashSet<>();

    int[][] states;

    MapLocation myLoc;
    boolean[] canMoveArray;
    int round;

    int turnsMovingToObstacle = 0;
    final int MAX_TURNS_MOVING_TO_OBSTACLE = 2;
    final int MIN_DIST_RESET = 3;

    void update(MapLocation target){
        if (!rc.isMovementReady()) return;
        myLoc = rc.getLocation();
        round = rc.getRoundNum();
        generateCanMove(target);
    }

    void generateCanMove(MapLocation target){
        canMoveArray = new boolean[9];
        for (Direction dir : dirs){
            switch (dir){
                case CENTER:
                    canMoveArray[dir.ordinal()] = true;
                    break;
                default:
                    canMoveArray[dir.ordinal()] = rc.canMove(dir);
                    break;
            }
            if (Robot.adjacentTiles.dirDanger[dir.ordinal()].closeToEnemyHQ) canMoveArray[dir.ordinal()] = false;
        }
        if (lastCurrent != null){
            int d = rc.getLocation().distanceSquaredTo(lastCurrent);
            if (d > 0 && d <= 2){
                lastObstacleFound = lastCurrent;
                Direction dirCurrent = rc.getLocation().directionTo(lastCurrent);
                canMoveArray[dirCurrent.ordinal()] = false;
            }
        }

        try {

            if (lastObstacleFound == null) {
                for (Direction dir : dirs) {
                    if (!canMoveArray[dir.ordinal()]) continue;
                    MapLocation newLoc = rc.getLocation().add(dir);
                    if (newLoc.distanceSquaredTo(target) <= 2) continue;
                    Direction cur = rc.senseMapInfo(newLoc).getCurrentDirection();
                    if (cur == null || cur == Direction.CENTER) continue;
                    MapLocation newLoc2 = newLoc.add(cur);
                    if (newLoc2.distanceSquaredTo(target) >= rc.getLocation().distanceSquaredTo(target)){
                        canMoveArray[dir.ordinal()] = false;
                    }
                }
            }
        } catch (GameActionException e){
            e.printStackTrace();
        }


    }

    void debugMovement(){
        try{
            for (Direction dir : dirs){
                MapLocation newLoc = myLoc.add(dir);
                if (rc.canSenseLocation(newLoc) && canMoveArray[dir.ordinal()]) rc.setIndicatorDot(newLoc, 0, 0, 255);
            }
        } catch (Throwable t){
            t.printStackTrace();
        }
    }

    void moveTo(MapLocation target){

        Robot.bytecodeDebug += "BC_BUG_BEGIN = " + Clock.getBytecodeNum() + " ";

        //No target? ==> bye!
        if (!rc.isMovementReady()) return;
        if (target == null) target = rc.getLocation();
        //if (Constants.DEBUG == 1)
        //rc.setIndicatorLine(rc.getLocation(), target, 255, 0, 255);

        update(target);
        //if (target == null) return;


        //different target? ==> previous data does not help!
        if (prevTarget == null){
            if (Constants.DEBUG_BUGPATH == 1) System.out.println("Previous target is null! reset!");
            resetPathfinding();
            rotateRight = null;
            //rotateRightAux = null;
        }


        else {
            int distTargets = target.distanceSquaredTo(prevTarget);
            if (distTargets > 0) {
                if (Constants.DEBUG_BUGPATH == 1) System.out.println("Different target!! Reset!");
                if (distTargets >= MIN_DIST_RESET){
                    rotateRight = null;
                    //rotateRightAux = null;
                    resetPathfinding();
                }
                else{
                    if (Constants.DEBUG_BUGPATH == 1) System.out.println("Different target!! Soft Reset!");
                    softReset(target);
                }
            }
        }

        Robot.bytecodeDebug += "BC_BUG_1 = " + Clock.getBytecodeNum() + " ";

        //Update data
        prevTarget = target;

        checkState();
        myLoc = rc.getLocation();

        Robot.bytecodeDebug += "BC_BUG_12 = " + Clock.getBytecodeNum() + " ";


        int d = myLoc.distanceSquaredTo(target);
        if (d == 0){
            return;
        }

        //If I'm at a minimum distance to the target, I'm free!
        if (d < minDistToTarget){
            if (Constants.DEBUG_BUGPATH == 1) System.out.println("resetting on d < mindist");
            resetPathfinding();
            minDistToTarget = d;
            minLocationToTarget = myLoc;
        }

        //If there's an obstacle I try to go around it [until I'm free] instead of going to the target directly
        Direction dir = myLoc.directionTo(target);
        if (lastObstacleFound == null){
            if (tryGreedyMove()){
                if (Constants.DEBUG_BUGPATH == 1) System.out.println("No obstacle and could move greedily :)");
                resetPathfinding();
                return;
            }
        }
        else{
            dir = myLoc.directionTo(lastObstacleFound);
            rc.setIndicatorDot(lastObstacleFound, 0, 255, 0);
            if (lastCurrent != null) rc.setIndicatorDot(lastCurrent, 255, 0, 0);
        }

        Robot.bytecodeDebug += "BC_BUG_2 = " + Clock.getBytecodeNum() + " ";

        try {

            if (canMoveArray[dir.ordinal()]){
                Robot.move(dir);
                if (lastObstacleFound != null) {
                    if (Constants.DEBUG_BUGPATH == 1) System.out.println("Could move to obstacle?!");
                    ++turnsMovingToObstacle;
                    lastObstacleFound = rc.getLocation().add(dir);
                    if (turnsMovingToObstacle >= MAX_TURNS_MOVING_TO_OBSTACLE){
                        if (Constants.DEBUG_BUGPATH == 1) System.out.println("obstacle reset!!");
                        resetPathfinding();
                    } else if (!rc.onTheMap(lastObstacleFound)){
                        if (Constants.DEBUG_BUGPATH == 1) System.out.println("obstacle reset!! - out of the map");
                        resetPathfinding();
                    }
                }
                return;
            } else turnsMovingToObstacle = 0;

            checkRotate(dir);

            if (Constants.DEBUG_BUGPATH == 1) System.out.println(rotateRight + " " + dir.name());

            Robot.bytecodeDebug += "BC_BUG = " + Clock.getBytecodeNum() + " ";

            //I rotate clockwise or counterclockwise (depends on 'rotateRight'). If I try to go out of the map I change the orientation
            //Note that we have to try at most 16 times since we can switch orientation in the middle of the loop. (It can be done more efficiently)
            int i = 16;
            while (i-- > 0) {
                if (canMoveArray[dir.ordinal()]) {
                    Robot.move(dir);
                    Robot.bytecodeDebug += "BC_BUG_END = " + i + " " + Clock.getBytecodeNum() + " ";
                    return;
                }
                MapLocation newLoc = myLoc.add(dir);
                if (!rc.onTheMap(newLoc)) rotateRight = !rotateRight;
                    //If I could not go in that direction and it was not outside of the map, then this is the latest obstacle found
                else lastObstacleFound = newLoc;
                if (rotateRight) dir = dir.rotateRight();
                else dir = dir.rotateLeft();
            }

            Robot.bytecodeDebug += "BC_BUG_END = " + i + " " + Clock.getBytecodeNum() + " ";

            if (canMoveArray[dir.ordinal()]){
                Robot.move(dir);
                return;
            }
        } catch (Throwable t){
            t.printStackTrace();
        }
    }

    boolean tryGreedyMove(){
        try {
            //if (rotateRightAux != null) return false;
            MapLocation myLoc = rc.getLocation();
            Direction dir = myLoc.directionTo(prevTarget);
            if (canMoveArray[dir.ordinal()]) {
                Robot.move(dir);
                return true;
            }
            int dist = myLoc.distanceSquaredTo(prevTarget);
            int dist1 = Constants.INF, dist2 = Constants.INF;
            Direction dir1 = dir.rotateRight();
            MapLocation newLoc = myLoc.add(dir1);
            if (canMoveArray[dir1.ordinal()]) dist1 = newLoc.distanceSquaredTo(prevTarget);
            Direction dir2 = dir.rotateLeft();
            newLoc = myLoc.add(dir2);
            if (canMoveArray[dir2.ordinal()]) dist2 = newLoc.distanceSquaredTo(prevTarget);
            if (dist1 < dist && dist1 < dist2) {
                //rotateRightAux = true;
                Robot.move(dir1);
                return true;
            }
            if (dist2 < dist && dist2 < dist1) {
                ;//rotateRightAux = false;
                Robot.move(dir2);
                return true;
            }
        } catch(Throwable t){
            t.printStackTrace();
        }
        return false;
    }

    //TODO: check remaining cases
    //TODO: move obstacle if can move to obstacle lol
    void checkRotate(Direction dir){
        if (rotateRight != null) return;
        Direction dirLeft = dir;
        Direction dirRight = dir;
        int i = 8;
        while (--i >= 0) {
            if (!canMoveArray[dirLeft.ordinal()]) dirLeft = dirLeft.rotateLeft();
            else break;
        }
        i = 8;
        while (--i >= 0){
            if (!canMoveArray[dirRight.ordinal()]) dirRight = dirRight.rotateRight();
            else break;
        }
        int distLeft = myLoc.add(dirLeft).distanceSquaredTo(prevTarget), distRight = myLoc.add(dirRight).distanceSquaredTo(prevTarget);
        if (distRight < distLeft) rotateRight = true;
        else rotateRight = false;
    }

    //clear some of the previous data
    void resetPathfinding(){
        if (Constants.DEBUG_BUGPATH == 1) System.out.println("reset!");
        lastObstacleFound = null;
        minDistToTarget = Constants.INF;
        ++bugPathIndex;
        turnsMovingToObstacle = 0;
    }

    void softReset(MapLocation target){
        if (rc.getType() == RobotType.AMPLIFIER){
            resetPathfinding();
            return;
        }
        if (Constants.DEBUG_BUGPATH == 1) System.out.println("soft reset!");
        if (minLocationToTarget != null) minDistToTarget = minLocationToTarget.distanceSquaredTo(target);
        else resetPathfinding();
    }

    void checkState(){
        if (!isReady()) return;
        if (lastObstacleFound == null) return;
        int state = (bugPathIndex << 14) | (lastObstacleFound.x << 8) |  (lastObstacleFound.y << 2);
        if (rotateRight != null) {
            if (rotateRight) state |= 1;
            else state |= 2;
        }
        if (states[myLoc.x][myLoc.y] == state){
            resetPathfinding();
        }

        states[myLoc.x][myLoc.y] = state;
    }

    void checkCurrent() throws GameActionException{
        if (lastObstacleFound == null){
            lastCurrent = null;
            return;
        }
        MapInfo mi = rc.senseMapInfo(rc.getLocation());
        if (mi.getCurrentDirection() == null || mi.getCurrentDirection() == Direction.CENTER){
            if (lastCurrent != null && lastObstacleFound.distanceSquaredTo(lastCurrent) == 0) return;
            lastCurrent = null;
            return;
        }
        lastCurrent = rc.getLocation();
    }

}