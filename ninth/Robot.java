package ninth;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public abstract class Robot {

    static RobotController rc;
    static HeadquarterComm hComm;

    static BFSManager bfsManager;

    static OtherComm otherComm;

    static Pathfinder pathfinder;

    Direction[] directions = Direction.values();
    static Explore explore;

    static VisibleTilesManager vt;

    RobotType myType;




    public Robot(RobotController rc) throws GameActionException {
        this.rc = rc; //this should always go first
        this.hComm = new HeadquarterComm();
        this.pathfinder = new Pathfinder();
        this.explore = new Explore();
        this.otherComm = new OtherComm(); //should go after explore
        this.bfsManager = new BFSManager();
        this.myType = rc.getType();
        this.vt = new VisibleTilesManager();
    }

    abstract void play() throws GameActionException;

    void initTurn() throws GameActionException {
        Constants.indicatorString = new String();
        otherComm.updateSymmetry();
        otherComm.checkEnemyComms();
        otherComm.reportClosestEnemyLauncher();
    }

    void endTurn() throws GameActionException {
        pathfinder.bugPath.checkCurrent();
        otherComm.reportClosestEnemyLauncher();
        explore.mapdata.run();
        explore.mapdata.reportMap();
        if (myType == RobotType.CARRIER){
            vt.run();
        }
        if (myType == RobotType.HEADQUARTERS){
            otherComm.checkSymmetry();
            otherComm.fillSymmetry();
        }
        else {
            bfsManager.run();
            otherComm.checkSymmetry();
            otherComm.fillSymmetry();
        }
        rc.setIndicatorString(Constants.indicatorString);
    }

}