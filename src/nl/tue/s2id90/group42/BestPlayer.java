package nl.tue.s2id90.group42;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tue.s2id90.draughts.DraughtsState;
import static nl.tue.s2id90.draughts.DraughtsState.BLACKKING;
import static nl.tue.s2id90.draughts.DraughtsState.BLACKPIECE;
import static nl.tue.s2id90.draughts.DraughtsState.EMPTY;
import static nl.tue.s2id90.draughts.DraughtsState.WHITEKING;
import static nl.tue.s2id90.draughts.DraughtsState.WHITEPIECE;
import nl.tue.s2id90.draughts.player.DraughtsPlayer;
import nl.tue.s2id90.game.GameState;
import org10x10.dam.game.Move;

/**
 *
 * @author s121924
 */
public class BestPlayer extends DraughtsPlayer {
    
    private boolean stop = false;
    private boolean isWhite;
    
    @Override
    public void stop() { stop = true; }
    
    public BestPlayer() {
        super(BestPlayer.class.getResource("resources/spongebob.png"));
    }

    @Override
    public Move getMove(DraughtsState s) {
        isWhite = s.isWhiteToMove();
        GameNode root = new GameNode(s);
	// find possible moves
        int depth = 0;
	int max = Integer.MIN_VALUE;
	
	if (root.moves.size() == 1) {
            root.setBestMove(root.moves.get(0));
        } else {
            //Iterative deepening
            while (! stop) {
		depth++;

		// try each possible move
		for (int i = 0; i < root.moves.size(); i++) {
                    ValuedMove move = root.moves.get(i);
					
                    // find value of move
                    s.doMove(move);
                    try {
                        move.value = alphaBeta(new GameNode(s), depth, max, Integer.MAX_VALUE, false);
                    } catch (AIStoppedException ex) {
                        return root.getBestMove();
                    }
                    s.undoMove(move);
                    // remember best move so far
                    if (move.value > max) {
                        root.moves.remove(i);
                        root.moves.add(0, move);

                        max = move.value;
                        root.setBestMove(move);
                    }
					
                }
            }
        }
	return root.getBestMove();
    }

    @Override
    public Integer getValue() {
        return 0;
    }
    
    public Integer alphaBeta(GameNode node, int depth, int alpha, int beta, boolean maximizingPlayer) throws AIStoppedException {
        if ( stop ) {
            stop = false;
            throw new AIStoppedException();
        }
        DraughtsState state = node.getState();
        List<ValuedMove> moves = node.moves;
        boolean capture = false;
        
        //no moves left
        if (moves.size() == 0) {
            return evaluate(state);
        }
        
        for (ValuedMove move : moves) {
            if(move.isCapture()){
                capture = true;
                break;
            }
        }
        
        if (depth == 0){
            if(capture){
                return alphaBeta(node, 1, alpha, beta, maximizingPlayer);
            } else {
                return evaluate(state);
            }
        }
        
        if (maximizingPlayer) {
            for (ValuedMove move : moves) {
                state.doMove(move);
                alpha = Math.max(alpha, alphaBeta(new GameNode(state), depth-1, alpha, beta, false));
                if (beta <= alpha)
                        break;
                state.undoMove(move);
            }
            return alpha;  
        }else{
            for (ValuedMove move : moves) {
                state.doMove(move);
                beta = Math.min(beta, alphaBeta(new GameNode(state), depth-1, alpha, beta, true));    
                if (beta <= alpha)
                    break;
                state.undoMove(move);
            }
            return beta;
        }
    }
    
    public int evaluate(DraughtsState state){
        int[] pieces = state.getPieces();
        int value = 0;
        if (isWhite) {
            for (int i = 0; i != pieces.length; i++) {
                boolean edge = false;
                if (i % 5 == 0 || i % 5 == 1) {
                    edge = true;
                }
                if (pieces[i] == WHITEPIECE) {
                    value += 1;
                    if (edge) {
                        if (i + 5 > 50) {
                            value += 1;
                        }
                        else if (pieces[i + 5] == WHITEPIECE || pieces[i + 5] == WHITEKING) {
                            value += 1;
                        }
                    }
                    else {
                        if (i + 5 > 50) {
                            value += 1;
                        }
                        else if (   pieces[i + 5] == WHITEPIECE || 
                                    pieces[i + 6] == WHITEPIECE || 
                                    pieces[i + 5] == WHITEKING || 
                                    pieces[i + 6] == WHITEKING) {
                            value += 1;
                        }
                    }
                } else if (pieces[i] == WHITEKING) {
                    value += 6;
                } else if (pieces[i] == BLACKPIECE) {
                    value -= 2;
                } else if (pieces[i] == BLACKKING) {
                    value -= 6;
                } 
            }
        } else {
            for (int i = 0; i != pieces.length; i++) {
                boolean edge = false;
                if (i % 5 == 0 || i % 5 == 1) {
                    edge = true;
                }
                if (pieces[i] == WHITEPIECE) {
                    value -= 2;
                } else if (pieces[i] == WHITEKING) {
                    value -= 6;
                } else if (pieces[i] == BLACKPIECE) {
                    value += 1;
                    if (edge) {
                        if (i - 5 < 1) {
                            value += 1;
                        }
                        else if (pieces[i - 5] == BLACKPIECE || pieces[i - 5] == BLACKKING) {
                            value += 1;
                        }
                    }
                    else {
                        if (i - 5 < 1) {
                            value += 1;
                        }
                        else if (   pieces[i - 5] == BLACKPIECE || 
                                    pieces[i - 6] == BLACKPIECE || 
                                    pieces[i - 5] == BLACKKING || 
                                    pieces[i - 6] == BLACKKING) {
                            value += 1;
                        }
                    }
                } else if (pieces[i] == BLACKKING) {
                    value += 6;
                } 
            }
        }
        
        return value;
    }
}
