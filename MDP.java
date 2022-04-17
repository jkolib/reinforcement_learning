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
    public static int bottom = Integer.MAX_VALUE;
    public static int top = Integer.MIN_VALUE;

    public static void main(String[] args){
        assert args.length == 1 : "no file name passed";
        read(args[0]);

        // playing rounds
        for(int i = 1; i <= rounds; i++){
            playRound();

            if(i % freq == 0 || i == rounds){ // printing round
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
                    // parsing first line of input file
                    case 1:
                        arr = line.split(" ");

                        nt_states = Integer.parseInt(arr[0]);
                        t_states = Integer.parseInt(arr[1]);
                        rounds = Integer.parseInt(arr[2]);
                        freq = Integer.parseInt(arr[3]);
                        M = Integer.parseInt(arr[4]);

                        // initializing count and total containers
                        count = new int[nt_states][];
                        total = new int[nt_states][];

                        // initializing nt_state objects
                        for(int j = 0; j < nt_states; j++){
                            nt_states_node.put(j, new Nt_state(j));
                        }

                        break;

                    // parsing second line of input file
                    case 2:
                        arr = line.split(" ");

                        for(int i = 0; i < arr.length; i++){
                            if(i % 2 == 0){ // storing t_state "nodes" in hashmap
                                int id = Integer.parseInt(arr[i]);
                                int reward = Integer.parseInt(arr[i+1]);
                                t_states_node.put(id, reward);

                                // finding max and min rewards
                                top = Math.max(reward, top);
                                bottom = Math.min(reward, bottom);
                            }
                        }

                        break;

                    // parsing info about decisions/probabilities
                    default:
                        String[] line_arr = line.split(" ");
                        String[] s_a = line_arr[0].split(":");

                        // identifying state and action number
                        int state_id = Integer.parseInt(s_a[0]);
                        int action_num = Integer.parseInt(s_a[1]);

                        // creating new action
                        Nt_state curr = nt_states_node.get(state_id);
                        Action action = new Action(action_num);

                        // adding probabilities to new action
                        for(int k = 1; k < line_arr.length; k++){
                            if(k % 2 == 0){
                                int nstate_id = Integer.parseInt(line_arr[k-1]);
                                double prob = Double.parseDouble(line_arr[k]);

                                action.add_prob(nstate_id, prob);
                            }
                        }

                        // adding new action to current nt_state's action list
                        curr.add_action(action);
                }

                line_n++;
                line = br.readLine();
            }

            // completing initialization of count and total containers
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
        // randomly choosing a starting state
        Random r = new Random();
        int id = r.nextInt(nt_states);

        // hashset for storing unique state and action combinations
        HashSet<ArrayList<Integer>> set = new HashSet<ArrayList<Integer>>();
        
        int reward = 0;

        // loops until terminal state reached
        while(true){
            // getting current state by id
            Nt_state state = nt_states_node.get(id);

            int action_num = chooseAction(id);

            // creating list for state action combination and adding to hashset
            ArrayList<Integer> s_a = new ArrayList<Integer>();
            s_a.add(state.id);
            s_a.add(action_num);
            set.add(s_a);

            // getting id of next state according to probabilities
            Action action = state.actions.get(action_num);
            id = nextState(action.probs);

            // exit when terminal state reached
            if(id >= nt_states){
                reward = t_states_node.get(id);
                break;
            }
        }

        // incrementing count and increasing total by reward amount
        for(ArrayList<Integer> sa : set){
            int state = sa.get(0);
            int action = sa.get(1);

            count[state][action]++;
            total[state][action] += reward;
        }
    }

    public static void printRound(int round_num){
        // parallel arrays for best values and their associated action number
        double[] best_vals = new double[nt_states];
        int[] best_act = new int[nt_states];

        System.out.println("After " + round_num + " rounds");

        // printing count values
        System.out.println("Count:");
        for(int state = 0; state < nt_states; state++){
            for(int action = 0; action < count[state].length; action++){
                System.out.print("["+state+","+action+"]="+count[state][action]+". ");

                // calculating best actions
                if(count[state][action] == 0){ // flag for when best unknown
                    best_vals[state] = -1;
                    best_act[state] = -1;
                }

                if(best_vals[state] == 0 && count[state][action] != 0){ // initializing best_vals and best_act
                    best_vals[state] = total[state][action] / (double) count[state][action];
                    best_act[state] = action;
                }
                else if(best_vals[state] != -1){ // replacing best_vals and best_act when better action found
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

        // printing total values
        System.out.println("Total:");
        for(int state = 0; state < nt_states; state++){
            for(int action = 0; action < total[state].length; action++){
                System.out.print("["+state+","+action+"]="+total[state][action]+". ");
            }
            System.out.println();
        }
        System.out.println();

        // printing best actions
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

        if(untried != -1){ // choosing untried action
            return untried;
        }

        double[] avg = new double[n];
        double[] savg = new double[n];

        // setting values in avg
        for(int i = 0; i < n; i++){
            avg[i] = total[id][i] / (double) count[id][i];
        }

        // setting values in savg
        for(int i = 0; i < n; i++){
            savg[i] = 0.25 + 0.75 * (avg[i] - bottom) / ((double) (top - bottom));
        }

        // setting c
        int c = 0;
        for(int i = 0; i < n; i++){
            c += count[id][i];
        }

        // setting values in up
        double[] up = new double[n];
        for(int i = 0; i < n; i++){
            up[i] = Math.pow(savg[i], c/ ((double) M));
        }

        // setting norm
        double norm = 0;
        for(int i = 0; i < n; i++){
            norm += up[i];
        }
        
        // setting values in p
        double[] p = new double[n];
        for(int i = 0; i < n; i++){
            p[i] = up[i] / norm;
        }

        return chooseFromDist(p);
    }

    public static int getUntried(int id){
        // searching for count[state][action] equal to 0 and returning action
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

        // setting values in u
        for(int i = 1; i < u.length; i++){
            u[i] = u[i-1] + p[i];
        }

        double x = Math.random();

        // returning choice
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
            // establishing range of accepted choice values, weights which state is returned
            double low = prob_sum;
            double high = prob_sum + probs.get(state);

            // returning state based on whether choice lies in range of its probability
            if(choice < high && choice >= low){
                return state;
            }

            prob_sum += probs.get(state);
        }

        return 0;
    }
}

// class for storing info about nonterminal states
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

// class for storing info about actions
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