package fourth;

import battlecode.common.*;

public abstract class Robot {

    static RobotController rc;
    static HeadquarterComm hComm;

    static BFSManager bfsManager;

    OtherComm otherComm;

    Pathfinder pathfinder;

    Direction[] directions = Direction.values();
    Explore explore;

    RobotType myType;




    public Robot(RobotController rc) throws GameActionException {
        this.rc = rc; //this should always go first
        this.hComm = new HeadquarterComm();
        this.pathfinder = new Pathfinder();
        this.explore = new Explore();
        this.otherComm = new OtherComm(); //should go after explore
        this.bfsManager = new BFSManager();
        this.myType = rc.getType();
    }

    abstract void play() throws GameActionException;

    void initTurn() throws GameActionException {
        Constants.indicatorString = new String();
        otherComm.updateSymmetry();
    }

    void endTurn() throws GameActionException {
        pathfinder.bugPath.checkCurrent();
        explore.mapdata.run();
        explore.mapdata.reportMap();
        if (myType == RobotType.CARRIER){
            Carrier c = (Carrier)this;
            c.im.run();
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