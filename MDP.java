import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class MDP{
    public static int nt_states;
    public static int t_states;
    public static int rounds;
    public static int freq;
    public static int M;
    public static HashMap<Integer, Integer> t_states_node = new HashMap<Integer, Integer>();
    public static HashMap<Integer, Nt_state> nt_states_node = new HashMap<Integer, Nt_state>();

    public static void main(String[] args){
        assert args.length == 1 : "no file name passed";
        read(args[0]);

        
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

            br.close();
        } catch (Exception e) {
            System.out.println("error reading from file: " + e);
        }
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