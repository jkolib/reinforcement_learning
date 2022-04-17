import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class MDP{
    public static int nt_states;
    public static int t_states;
    public static int rounds;
    public static int freq;
    public static int M;
    public static HashMap<Integer, Integer> t_states_node = new HashMap<Integer, Integer>();
    public static HashMap<Integer, Nt_state> nt_states_node = new HashMap<Integer, Nt_state>();
    public static int[][] count;
    public static int[][] total;
    public static int bottom = -1;
    public static int top = -1;

    public static void main(String[] args){
        assert args.length == 1 : "no file name passed";
        read(args[0]);

        for(int i = 1; i <= rounds; i++){
            playRound();

            if(i % freq == 0 || i == rounds){
                printRound(i);
            }
        }
    }

    public static void read(String fname){
        try {
            BufferedReader br = new BufferedReader(new FileReader(fname));

            int line_n = 1;
            String[] arr;
            String line = br.readLine();
            while(line != null){
                switch(line_n){
                    case 1:
                        arr = line.split(" ");

                        nt_states = Integer.parseInt(arr[0]);
                        t_states = Integer.parseInt(arr[1]);
                        rounds = Integer.parseInt(arr[2]);
                        freq = Integer.parseInt(arr[3]);
                        M = Integer.parseInt(arr[4]);

                        count = new int[nt_states][];
                        total = new int[nt_states][];

                        for(int j = 0; j < nt_states; j++){
                            nt_states_node.put(j, new Nt_state(j));
                        }

                        break;

                    case 2:
                        arr = line.split(" ");

                        for(int i = 0; i < arr.length; i++){
                            if(i % 2 == 0){
                                int id = Integer.parseInt(arr[i]);
                                int reward = Integer.parseInt(arr[i+1]);
                                t_states_node.put(id, reward);

                                if(bottom == -1){
                                    bottom = reward;
                                    top = reward;
                                }
                                else{
                                    top = Math.max(reward, top);
                                    bottom = Math.min(reward, bottom);
                                }
                            }
                        }

                        break;

                    default:
                        String[] line_arr = line.split(" ");
                        String[] s_a = line_arr[0].split(":");

                        int state_id = Integer.parseInt(s_a[0]);
                        int action_num = Integer.parseInt(s_a[1]);

                        Nt_state curr = nt_states_node.get(state_id);
                        Action action = new Action(action_num);

                        for(int k = 1; k < line_arr.length; k++){
                            if(k % 2 == 0){
                                int nstate_id = Integer.parseInt(line_arr[k-1]);
                                double prob = Double.parseDouble(line_arr[k]);

                                action.add_prob(nstate_id, prob);
                            }
                        }

                        curr.add_action(action);
                }

                line_n++;
                line = br.readLine();
            }

            for(int id : nt_states_node.keySet()){
                int num_actions = nt_states_node.get(id).actions.size();
                count[id] = new int[num_actions];
                total[id] = new int[num_actions];
            }

            br.close();
        } catch(Exception e) {
            System.out.println("error reading from file: " + e);
        }
    }

    public static void playRound(){
        Random r = new Random();
        int id = r.nextInt(nt_states);

        HashSet<ArrayList<Integer>> set = new HashSet<ArrayList<Integer>>();
        
        int reward = 0;

        while(true){
            Nt_state state = nt_states_node.get(id);

            int action_num = chooseAction(id);

            ArrayList<Integer> s_a = new ArrayList<Integer>();
            s_a.add(state.id);
            s_a.add(action_num);
            set.add(s_a);

            Action action = state.actions.get(action_num);
            id = nextState(action.probs);

            if(id >= nt_states){
                reward = t_states_node.get(id);
                break;
            }
        }

        for(ArrayList<Integer> s__a : set){
            int state = s__a.get(0);
            int action = s__a.get(1);

            count[state][action]++;
            total[state][action] += reward;
        }
    }

    public static void printRound(int round_num){
        double[] best_vals = new double[nt_states];
        int[] best_act = new int[nt_states];

        System.out.println("After " + round_num + " rounds");

        System.out.println("Count:");
        for(int state = 0; state < nt_states; state++){
            for(int action = 0; action < count[state].length; action++){
                System.out.print("["+state+","+action+"]="+count[state][action]+". ");

                if(count[state][action] == 0){
                    best_vals[state] = -1;
                    best_act[state] = -1;
                }

                if(best_vals[state] == 0 && count[state][action] != 0){
                    best_vals[state] = total[state][action] / (double) count[state][action];
                    best_act[state] = action;
                }
                else if(best_vals[state] != -1){
                    double val = total[state][action] / (double) count[state][action];

                    if(val > best_vals[state]){
                        best_vals[state] = val;
                        best_act[state] = action;
                    }
                }
            }
            System.out.println();
        }
        System.out.println();

        System.out.println("Total:");
        for(int state = 0; state < nt_states; state++){
            for(int action = 0; action < total[state].length; action++){
                System.out.print("["+state+","+action+"]="+total[state][action]+". ");
            }
            System.out.println();
        }
        System.out.println();

        System.out.print("Best action: ");
        for(int state = 0; state < nt_states; state++){
            if(best_act[state] != -1){
                System.out.print(state+":"+best_act[state]+". ");
            }
            else{
                System.out.print(state+":U. ");
            }
        }
        System.err.println("\n");
    }

    public static int chooseAction(int id){
        Nt_state state = nt_states_node.get(id);

        int n = state.actions.size();
        int untried = getUntried(id);

        if(untried != -1){
            return untried;
        }

        double[] avg = new double[n];
        double[] savg = new double[n];

        for(int i = 0; i < n; i++){
            avg[i] = total[id][i] / count[id][i];
        }

        for(int i = 0; i < n; i++){
            savg[i] = 0.25 + 0.75 * (avg[i] - bottom) / (top - bottom);
        }

        int c = 0;
        for(int i = 0; i < n; i++){
            c += count[id][i];
        }

        double[] up = new double[n];
        for(int i = 0; i < n; i++){
            up[i] = Math.pow(savg[i], c/M);
        }

        double norm = 0;
        for(int i = 0; i < n; i++){
            norm += up[i];
        }
        
        double[] p = new double[n];
        for(int i = 0; i < n; i++){
            p[i] = up[i] / norm;
        }

        return chooseFromDist(p);
    }

    public static int getUntried(int id){
        for(int i = 0; i < count[id].length; i++){
            if(count[id][i] == 0){
                return i;
            }
        }

        return -1;
    }

    public static int chooseFromDist(double[] p){
        double[] u = new double[p.length];
        u[0] = p[0];

        for(int i = 1; i < u.length; i++){
            u[i] = u[i-1] + p[i];
        }

        double x = Math.random();

        for(int i = 0; i < u.length-1; i++){
            if(x < u[i]){
                return i;
            }
        }

        return u.length-1;
    }

    public static int nextState(HashMap<Integer, Double> probs){
        double choice = Math.random();

        double prob_sum = 0;
        for(int state : probs.keySet()){
            double low = prob_sum;
            double high = prob_sum + probs.get(state);

            if(choice < high && choice >= low){
                return state;
            }

            prob_sum += probs.get(state);
        }

        return -1;
    }
}

// class T_state{
//     public int id;
//     public int reward;

//     public T_state(int id, int reward){
//         this.id = id;
//         this.reward = reward;
//     }
// }

class Nt_state{
    public int id;
    public ArrayList<Action> actions = new ArrayList<Action>();

    public Nt_state(int id){
        this.id = id;
    }

    public void add_action(Action action){
        this.actions.add(action);
    }
}

class Action{
    public int num;
    public HashMap<Integer, Double> probs = new HashMap<Integer, Double>();

    public Action(int num){
        this.num = num;
    }

    public void add_prob(int state_id, double prob){
        this.probs.put(state_id, prob);
    }
}