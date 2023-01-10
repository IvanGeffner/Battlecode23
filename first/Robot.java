package first;
import battlecode.common.*;

public abstract class Robot {

    static RobotController rc;
    HeadquarterComm hComm;

    Pathfinder pathfinder;




    public Robot(RobotController rc) throws GameActionException {
        this.rc = rc; //this should always go first
        this.hComm = new HeadquarterComm();
        this.pathfinder = new Pathfinder();
    }

    abstract void play() throws GameActionException;

    void initTurn(){
    }

    void endTurn(){
    }

}