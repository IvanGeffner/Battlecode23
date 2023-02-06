package fortytwo;

import battlecode.common.Clock;
import battlecode.common.MapLocation;

public class BFSGreedy {



    BFS trueBFS;

    boolean reset = true;
    boolean init = false;

    int queueIndexBeginning = 0;
    int queueIndexEnd = 0;

    int[] vars;

    static int[] mapInfo;
    static int H, W;

    static int H1, W1;

    static int diffE, diffNE, diffN, diffNW, diffW, diffSW, diffS, diffSE;

    static final int MAX_BYTECODE = 1200;

    BFSGreedy(BFS trueBFS){
        this.trueBFS = trueBFS;
        H = MapData.H;
        W = MapData.W;

        this.mapInfo = MapData.mapInfo;

        H1 = H - 1;
        W1 = W - 1;

        diffE = H;
        diffNE = H+1;
        diffN = 1;
        diffNW = 1-H;
        diffW = -H;
        diffSW = -H-1;
        diffS = -1;
        diffSE = H-1;
    }

    void init(){
        init = true;
        vars = new int[trueBFS.vars.length];
    }

    void reset(){
        reset = true;
    }

    void applyReset(){
        Constants.indicatorString += "RESETTINGGGG ";
        queueIndexBeginning = trueBFS.queueIndexBeginning;
        queueIndexEnd = trueBFS.queueIndexEnd;
        System.arraycopy(trueBFS.vars, 0, vars, 0, trueBFS.vars.length);
        reset = false;
    }

    int getDistance(MapLocation m){
        if (reset) return 0;
        int aux = m.x*H + m.y;
        return (vars[aux] & 0xFFFF);
    }

    MapLocation getFront(){
        if (reset) return null;
        if (queueIndexBeginning >= queueIndexEnd) return null;
        int code = (vars[queueIndexBeginning] >>> 16)&0xFFFF;
        return new MapLocation(code/H, code%H);
    }

    static int x,y,aux, dist;

    boolean fillWithoutNeutralTwo(int code){
        x = code/H; y = code%H;
        Robot.rc.setIndicatorDot(new MapLocation(x,y), 0 , 0, 255);

        /*********************************************** NE ****************************************************/
        if (x < W1 && y < H1){
            aux = code + diffNE;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 6:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    fillWithoutChecks(aux);
                    return true;
                default:
            }
        }

        /*********************************************** E ****************************************************/
        if (x < W1){
            aux = code + diffE;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 5:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    fillWithoutChecks(aux);
                    return true;
                default:
            }
        }

        /*********************************************** SE ****************************************************/
        if (x < W1 && y > 0){
            aux = code + diffSE;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 4:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    fillWithoutChecks(aux);
                    return true;
                default:
            }
        }


        /*********************************************** N ****************************************************/
        if (y < H1){
            aux = code + diffN;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 7:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    fillWithoutChecks(aux);
                    return true;
                default:
            }
        }




        /*********************************************** S ****************************************************/
        if (y > 0){
            aux = code + diffS;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 3:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    fillWithoutChecks(aux);
                    return true;
                default:
            }
        }


        /*********************************************** NW ****************************************************/
        if (x > 0 && y < H1){
            aux = code + diffNW;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 8:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    fillWithoutChecks(aux);
                    return true;
                default:
            }
        }



        /*********************************************** W ****************************************************/
        if (x > 0){
            aux = code + diffW;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 1:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    fillWithoutChecks(aux);
                    return true;
                default:
            }
        }
        return true;
    }

    boolean fillWithoutNeutral(int code){
        x = code/H; y = code%H;
        Robot.rc.setIndicatorDot(new MapLocation(x,y), 0, 0, 255);

        /*********************************************** NE ****************************************************/
        if (x < W1 && y < H1){
            aux = code + diffNE;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 6:
                    if (!fillWithoutNeutralTwo(aux)) return false;
                    return true;
                default:
            }
        }

        /*********************************************** E ****************************************************/
        if (x < W1){
            aux = code + diffE;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 5:
                    if (!fillWithoutNeutralTwo(aux)) return false;
                    return true;
                default:
            }
        }

        /*********************************************** SE ****************************************************/
        if (x < W1 && y > 0){
            aux = code + diffSE;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 4:
                    if (!fillWithoutNeutralTwo(aux)) return false;
                    return true;
                default:
            }
        }


        /*********************************************** N ****************************************************/
        if (y < H1){
            aux = code + diffN;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 7:
                    if (!fillWithoutNeutralTwo(aux)) return false;
                    return true;
                default:
            }
        }




        /*********************************************** S ****************************************************/
        if (y > 0){
            aux = code + diffS;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 3:
                    if (!fillWithoutNeutralTwo(aux)) return false;
                    return true;
                default:
            }
        }


        /*********************************************** NW ****************************************************/
        if (x > 0 && y < H1){
            aux = code + diffNW;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 8:
                    if (!fillWithoutNeutralTwo(aux)) return false;
                    return true;
                default:
            }
        }



        /*********************************************** W ****************************************************/
        if (x > 0){
            aux = code + diffW;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 1:
                    if (!fillWithoutNeutralTwo(aux)) return false;
                    return true;
                default:
            }
        }
        return true;
    }

    boolean fillWithNeutralTwo(int code){
        x = code/H; y = code%H;
        Robot.rc.setIndicatorDot(new MapLocation(x,y), 0, 0, 255);

        //int b = Clock.getBytecodeNum();

        /*********************************************** NE ****************************************************/
        if (x < W1 && y < H1){
            aux = code + diffNE;

            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 6:
                    fillWithoutChecks(aux);
                    x = code/H; y = code%H;
                    aux = code + diffNE;
                default:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    break;
            }
        }

        /*********************************************** E ****************************************************/
        if (x < W1){
            aux = code + diffE;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 5:
                    fillWithoutChecks(aux);
                    x = code/H; y = code%H;
                    aux = code + diffE;
                default:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    break;
            }
        }

        /*********************************************** SE ****************************************************/
        if (x < W1 && y > 0){
            aux = code + diffSE;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 4:
                    fillWithoutChecks(aux);
                    x = code/H; y = code%H;
                    aux = code + diffSE;
                default:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    break;
            }
        }


        /*********************************************** N ****************************************************/
        if (y < H1){
            aux = code + diffN;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 7:
                    fillWithoutChecks(aux);
                    x = code/H; y = code%H;
                    aux = code + diffN;
                default:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    break;
            }
        }




        /*********************************************** S ****************************************************/
        if (y > 0){
            aux = code + diffS;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 3:
                    fillWithoutChecks(aux);
                    x = code/H; y = code%H;
                    aux = code + diffS;
                default:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    break;
            }
        }


        /*********************************************** NW ****************************************************/
        if (x > 0 && y < H1){
            aux = code + diffNW;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 8:
                    fillWithoutChecks(aux);
                    x = code/H; y = code%H;
                    aux = code + diffNW;
                default:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    break;
            }
        }



        /*********************************************** W ****************************************************/
        if (x > 0){
            aux = code + diffW;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 1:
                    fillWithoutChecks(aux);
                    x = code/H; y = code%H;
                    aux = code + diffW;
                default:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    break;
            }
        }


        /*********************************************** SW ****************************************************/
        if (x > 0 && y > 0){
            aux = code + diffSW;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 2:
                    fillWithoutChecks(aux);
                    x = code/H; y = code%H;
                    aux = code + diffSW;
                default:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    break;
            }
        }
        return true;
    }

    boolean fillWithNeutral(int code){
        x = code/H; y = code%H;
        Robot.rc.setIndicatorDot(new MapLocation(x,y), 0, 0, 255);

        //int b = Clock.getBytecodeNum();

        /*********************************************** NE ****************************************************/
        if (x < W1 && y < H1){
            aux = code + diffNE;

            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 6:
                    if (!fillWithNeutralTwo(aux)) return false;
                    x = code/H; y = code%H;
                    aux = code + diffNE;
                default:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    break;
            }
        }

        /*********************************************** E ****************************************************/
        if (x < W1){
            aux = code + diffE;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 5:
                    if (!fillWithNeutralTwo(aux)) return false;
                    x = code/H; y = code%H;
                    aux = code + diffE;
                default:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    break;
            }
        }

        /*********************************************** SE ****************************************************/
        if (x < W1 && y > 0){
            aux = code + diffSE;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 4:
                    if (!fillWithNeutralTwo(aux)) return false;
                    x = code/H; y = code%H;
                    aux = code + diffSE;
                default:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    break;
            }
        }


        /*********************************************** N ****************************************************/
        if (y < H1){
            aux = code + diffN;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 7:
                    if (!fillWithNeutralTwo(aux)) return false;
                    x = code/H; y = code%H;
                    aux = code + diffN;
                default:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    break;
            }
        }




        /*********************************************** S ****************************************************/
        if (y > 0){
            aux = code + diffS;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 3:
                    if (!fillWithNeutralTwo(aux)) return false;
                    x = code/H; y = code%H;
                    aux = code + diffS;
                default:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    break;
            }
        }


        /*********************************************** NW ****************************************************/
        if (x > 0 && y < H1){
            aux = code + diffNW;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 8:
                    if (!fillWithNeutralTwo(aux)) return false;
                    x = code/H; y = code%H;
                    aux = code + diffNW;
                default:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    break;
            }
        }



        /*********************************************** W ****************************************************/
        if (x > 0){
            aux = code + diffW;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 1:
                    if (!fillWithNeutralTwo(aux)) return false;
                    x = code/H; y = code%H;
                    aux = code + diffW;
                default:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    break;
            }
        }


        /*********************************************** SW ****************************************************/
        if (x > 0 && y > 0){
            aux = code + diffSW;
            switch((mapInfo[aux/4] >>> (4*(aux%4))) & 0xF){
                case 10:
                case 12: break;
                case 2:
                    if (!fillWithNeutralTwo(aux)) return false;
                    x = code/H; y = code%H;
                    aux = code + diffSW;
                default:
                    switch (vars[aux]&0xFFFF){
                        case 0:
                            vars[aux] |= dist;
                            vars[queueIndexEnd++] |= (aux << 16);
                            break;
                    }
                    break;
            }
        }
        return true;
    }

    void fillWithoutChecks(int code){
        x = code/H; y = code%H;

        //int b = Clock.getBytecodeNum();

        /*********************************************** NE ****************************************************/
        if (x < W1 && y < H1){
            aux = code + diffNE;
            switch (vars[aux]&0xFFFF){
                case 0:
                    vars[aux] |= dist;
                    vars[queueIndexEnd++] |= (aux << 16);
                    break;
            }
        }

        /*********************************************** E ****************************************************/
        if (x < W1){
            aux = code + diffE;
            switch (vars[aux]&0xFFFF){
                case 0:
                    vars[aux] |= dist;
                    vars[queueIndexEnd++] |= (aux << 16);
                    break;
            }
        }

        /*********************************************** SE ****************************************************/
        if (x < W1 && y > 0){
            aux = code + diffSE;
            switch (vars[aux]&0xFFFF){
                case 0:
                    vars[aux] |= dist;
                    vars[queueIndexEnd++] |= (aux << 16);
                    break;
            }
        }


        /*********************************************** N ****************************************************/
        if (y < H1){
            aux = code + diffN;
            switch (vars[aux]&0xFFFF){
                case 0:
                    vars[aux] |= dist;
                    vars[queueIndexEnd++] |= (aux << 16);
                    break;
            }
        }




        /*********************************************** S ****************************************************/
        if (y > 0){
            aux = code + diffS;
            switch (vars[aux]&0xFFFF){
                case 0:
                    vars[aux] |= dist;
                    vars[queueIndexEnd++] |= (aux << 16);
                    break;
            }
        }


        /*********************************************** NW ****************************************************/
        if (x > 0 && y < H1){
            aux = code + diffNW;
            switch (vars[aux]&0xFFFF){
                case 0:
                    vars[aux] |= dist;
                    vars[queueIndexEnd++] |= (aux << 16);
                    break;
            }
        }



        /*********************************************** W ****************************************************/
        if (x > 0){
            aux = code + diffW;
            switch (vars[aux]&0xFFFF){
                case 0:
                    vars[aux] |= dist;
                    vars[queueIndexEnd++] |= (aux << 16);
                    break;
            }
        }


        /*********************************************** SW ****************************************************/
        if (x > 0 && y > 0){
            aux = code + diffSW;
            switch (vars[aux]&0xFFFF){
                case 0:
                    vars[aux] |= dist;
                    vars[queueIndexEnd++] |= (aux << 16);
                    break;
            }
        }
        //queueIndexBeginning++;
    }

    void runBFS(){
        if (reset) return;
        int code;
        boolean includeNeutral;
        Constants.indicatorString += "  TRYING TO GO " + Clock.getBytecodeNum() + " ";
        while(queueIndexEnd > queueIndexBeginning){
            if (Clock.getBytecodesLeft() < MAX_BYTECODE) return;
            //int b = Clock.getBytecodeNum();
            includeNeutral = true;
            code = (vars[queueIndexBeginning] >>> 16)&0xFFFF;
            switch((mapInfo[code/4] >>> (4*(code%4))) & 0xF) {
                case 10:
                case 12:
                    if (queueIndexBeginning > 0) {
                        //Constants.indicatorString += "  RIP PATH  ";
                        ++queueIndexBeginning;
                        continue;
                    }
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    includeNeutral = false;
                    break;
            }

            dist = (vars[code]&0xFFFF) + 1;

            if (includeNeutral){
                if (fillWithNeutral(code)) ++queueIndexBeginning;
                else return;
            }
            else{
                if (fillWithoutNeutral(code)) ++queueIndexBeginning;
                else return;
            }
            //Constants.indicatorString += "PATH_BC = " + (Clock.getBytecodeNum() - b);
        }
    }

}
