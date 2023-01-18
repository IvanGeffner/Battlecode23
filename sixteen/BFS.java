package sixteen;

import battlecode.common.Clock;
import battlecode.common.MapLocation;

public class BFS {

    int[][] dists;
    int[][] queue;
    int distIndex = 0;
    int queueIndexBeginning = 0;
    int queueIndexEnd = 1;

    static int[] mapInfo;
    static int H, W;

    MapLocation start;

    static final int MAX_BYTECODE = 500;

    BFS(MapLocation start){
        H = MapData.H;
        W = MapData.W;
        dists = new int[W][];
        queue = new int[W][];
        this.mapInfo = MapData.mapInfo;
        this.start = start;
    }

    void run(){
        if (!isReady()) fill();
        runBFS();
    }

    boolean isReady(){
        return distIndex >= W;
    }

    void fill(){
        while (distIndex < W){
            if (Clock.getBytecodesLeft() < MAX_BYTECODE) return;
            dists[distIndex] = new int[H];
            queue[distIndex++] = new int[H];
        }
        dists[start.x][start.y] = 1;
        queue[0][0] = start.x*H+start.y;
    }

    MapLocation getFront(){
        if (!isReady()) return null;
        if (queueIndexBeginning >= queueIndexEnd) return null;
        int code = queue[queueIndexBeginning/H][queueIndexBeginning%H];
        return new MapLocation(code/H, code%H);
    }

    void runBFS(){
        int newX, newY;
        int newnewX, newnewY;
        int aux, newCode;
        while(queueIndexEnd > queueIndexBeginning){
            if (Clock.getBytecodesLeft() < MAX_BYTECODE) return;
            int code = queue[queueIndexBeginning/H][queueIndexBeginning%H];
            int mapCode = (mapInfo[code/4] >>> (4*(code%4))) & 0xF;
            int x = code/H, y = code%H;
            switch(mapCode) {
                case 0:
                    Robot.rc.setIndicatorDot(new MapLocation(x, y), 255, 0, 0);
                    return;
                //case 10:
                //case 12:
                    //++queueIndexBeginning;
                    //continue;

            }
            Robot.rc.setIndicatorDot(new MapLocation(x,y), 0, 0, 255);
            int dist = dists[x][y] + 1;

            /*********************************************** NE ****************************************************/
            newX = x+1; newY = y+1;
            if (newX < W && newX >= 0 && newY < H && newY >= 0){
                aux = newX*H+newY;
                newCode = (mapInfo[aux/4] >>> (4*(aux%4))) & 0xF;
                switch(newCode){
                    case 0: return;
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 8:
                    case 10:
                    case 12: break;
                    case 5:
                    case 7:
                    case 11:
                    case 13:
                    case 14:
                    case 15:
                    case 9:
                        if (dists[newX][newY] == 0){
                            dists[newX][newY] = dist;
                            queue[queueIndexEnd/H][queueIndexEnd%H] = aux;
                            queueIndexEnd++;
                        }
                        else if (dist < dists[newX][newY]) dists[newX][newY] = dist;
                        break;
                    case 6:
                        newnewX = newX+1; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }
                        newnewX = newX+1; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX+1; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }
                        break;
                }
            }




            /*********************************************** E ****************************************************/
            newX = x+1; newY = y;
            if (newX < W && newX >= 0 && newY < H && newY >= 0){
                aux = newX*H+newY;
                newCode = (mapInfo[aux/4] >>> (4*(aux%4))) & 0xF;
                switch(newCode){
                    case 0: return;
                    case 1:
                    case 2:
                    case 8:
                    case 10:
                    case 12: break;
                    case 4:
                    case 6:
                    case 7:
                    case 3:
                    case 11:
                    case 13:
                    case 14:
                    case 15:
                    case 9:
                        if (dists[newX][newY] == 0){
                            dists[newX][newY] = dist;
                            queue[queueIndexEnd/H][queueIndexEnd%H] = aux;
                            queueIndexEnd++;
                        }
                        else if (dist < dists[newX][newY]) dists[newX][newY] = dist;
                        break;
                    case 5:
                        newnewX = newX+1; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }
                        newnewX = newX+1; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX+1; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }
                        break;
                }
            }

            /*********************************************** SE ****************************************************/
            newX = x+1; newY = y-1;
            if (newX < W && newX >= 0 && newY < H && newY >= 0){
                aux = newX*H+newY;
                newCode = (mapInfo[aux/4] >>> (4*(aux%4))) & 0xF;
                switch(newCode){
                    case 0: return;
                    case 1:
                    case 2:
                    case 6:
                    case 7:
                    case 8:
                    case 10:
                    case 12: break;
                    case 5:
                    case 3:
                    case 11:
                    case 13:
                    case 14:
                    case 15:
                    case 9:
                        if (dists[newX][newY] == 0){
                            dists[newX][newY] = dist;
                            queue[queueIndexEnd/H][queueIndexEnd%H] = aux;
                            queueIndexEnd++;
                        }
                        else if (dist < dists[newX][newY]) dists[newX][newY] = dist;
                        break;
                    case 4:
                        newnewX = newX+1; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }
                        newnewX = newX+1; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX+1; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }
                        break;
                }
            }


            /*********************************************** N ****************************************************/
            newX = x; newY = y+1;
            if (newX < W && newX >= 0 && newY < H && newY >= 0){
                aux = newX*H+newY;
                newCode = (mapInfo[aux/4] >>> (4*(aux%4))) & 0xF;
                switch(newCode){
                    case 0: return;
                    case 2:
                    case 3:
                    case 4:
                    case 10:
                    case 12: break;
                    case 1:
                    case 5:
                    case 6:
                    case 8:
                    case 11:
                    case 13:
                    case 14:
                    case 15:
                    case 9:
                        if (dists[newX][newY] == 0){
                            dists[newX][newY] = dist;
                            queue[queueIndexEnd/H][queueIndexEnd%H] = aux;
                            queueIndexEnd++;
                        }
                        else if (dist < dists[newX][newY]) dists[newX][newY] = dist;
                        break;
                    case 7:
                        newnewX = newX+1; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }
                        newnewX = newX+1; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX+1; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }
                        break;
                }
            }




            /*********************************************** S ****************************************************/
            newX = x; newY = y-1;
            if (newX < W && newX >= 0 && newY < H && newY >= 0){
                aux = newX*H+newY;
                newCode = (mapInfo[aux/4] >>> (4*(aux%4))) & 0xF;
                switch(newCode){
                    case 0: return;
                    case 6:
                    case 7:
                    case 8:
                    case 10:
                    case 12: break;
                    case 1:
                    case 2:
                    case 4:
                    case 5:
                    case 11:
                    case 13:
                    case 14:
                    case 15:
                    case 9:
                        if (dists[newX][newY] == 0){
                            dists[newX][newY] = dist;
                            queue[queueIndexEnd/H][queueIndexEnd%H] = aux;
                            queueIndexEnd++;
                        }
                        else if (dist < dists[newX][newY]) dists[newX][newY] = dist;
                        break;
                    case 3:
                        newnewX = newX+1; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }
                        newnewX = newX+1; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX+1; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }
                        break;
                }
            }


            /*********************************************** NW ****************************************************/
            newX = x-1; newY = y+1;
            if (newX < W && newX >= 0 && newY < H && newY >= 0){
                aux = newX*H+newY;
                newCode = (mapInfo[aux/4] >>> (4*(aux%4))) & 0xF;
                switch(newCode){
                    case 0: return;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 10:
                    case 12: break;
                    case 1:
                    case 7:
                    case 11:
                    case 13:
                    case 14:
                    case 15:
                    case 9:
                        if (dists[newX][newY] == 0){
                            dists[newX][newY] = dist;
                            queue[queueIndexEnd/H][queueIndexEnd%H] = aux;
                            queueIndexEnd++;
                        }
                        else if (dist < dists[newX][newY]) dists[newX][newY] = dist;
                        break;
                    case 8:
                        newnewX = newX+1; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }
                        newnewX = newX+1; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX+1; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }
                        break;
                }
            }



            /*********************************************** W ****************************************************/
            newX = x-1; newY = y;
            if (newX < W && newX >= 0 && newY < H && newY >= 0){
                aux = newX*H+newY;
                newCode = (mapInfo[aux/4] >>> (4*(aux%4))) & 0xF;
                switch(newCode){
                    case 0: return;
                    case 4:
                    case 5:
                    case 6:
                    case 10:
                    case 12: break;
                    case 2:
                    case 3:
                    case 7:
                    case 8:
                    case 11:
                    case 13:
                    case 14:
                    case 15:
                    case 9:
                        if (dists[newX][newY] == 0){
                            dists[newX][newY] = dist;
                            queue[queueIndexEnd/H][queueIndexEnd%H] = aux;
                            queueIndexEnd++;
                        }
                        else if (dist < dists[newX][newY]) dists[newX][newY] = dist;
                        break;
                    case 1:
                        newnewX = newX+1; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }
                        newnewX = newX+1; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX+1; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }
                        break;
                }
            }


            /*********************************************** SW ****************************************************/
            newX = x-1; newY = y-1;
            if (newX < W && newX >= 0 && newY < H && newY >= 0){
                aux = newX*H+newY;
                newCode = (mapInfo[aux/4] >>> (4*(aux%4))) & 0xF;
                switch(newCode){
                    case 0: return;
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 10:
                    case 12: break;
                    case 1:
                    case 3:
                    case 11:
                    case 13:
                    case 14:
                    case 15:
                    case 9:
                        if (dists[newX][newY] == 0){
                            dists[newX][newY] = dist;
                            queue[queueIndexEnd/H][queueIndexEnd%H] = aux;
                            queueIndexEnd++;
                        }
                        else if (dist < dists[newX][newY]) dists[newX][newY] = dist;
                        break;
                    case 2:
                        newnewX = newX+1; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }
                        newnewX = newX+1; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX+1; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY+1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }

                        newnewX = newX-1; newnewY = newY-1;
                        if (newnewX < W && newnewX >= 0 && newnewY < H && newnewY >= 0){
                            if (dists[newnewX][newnewY] == 0){
                                dists[newnewX][newnewY] = dist;
                                queue[queueIndexEnd/H][queueIndexEnd%H] = newnewX*H + newnewY;
                                queueIndexEnd++;
                            }
                            else if (dist < dists[newnewX][newnewY]) dists[newnewX][newnewY] = dist;
                        }
                        break;
                }
            }
            queueIndexBeginning++;
        }
    }

}
