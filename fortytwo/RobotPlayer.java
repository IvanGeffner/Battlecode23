package fortytwo;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public strictfp class RobotPlayer {
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        Robot r;
        switch (rc.getType()) {
            case HEADQUARTERS:  r = new Headquarters(rc);  break;
            case CARRIER: r = new Carrier(rc);  break;
            case LAUNCHER: r = new Launcher(rc);  break;
            case BOOSTER: r = new Booster(rc);  break;
            case DESTABILIZER: r = new Destabilizer(rc);  break;
            default: r = new Amplifier(rc);   break;
        }

        while(true){
            int round = rc.getRoundNum();
            r.initTurn();
            Robot.bytecodeDebug += " BCINIT = " + Clock.getBytecodeNum();
            r.play();
            Robot.bytecodeDebug += "  BCPLAY = " + Clock.getBytecodeNum();
            r.endTurn();
            int roundEnd = rc.getRoundNum();
            if (round < roundEnd){
                System.out.println("FAIL " + round + " " + rc.getLocation().toString() + Robot.bytecodeDebug);
            }
            Robot.bytecodeDebug = new String();
            Constants.indicatorString = new String();
            Clock.yield();
        }
    }
}
