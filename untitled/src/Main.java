




class count{

}
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static int countocc(String input) {
        int n = 0;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '1') {
                n += 1;
            }
        }

        int mini = 4;


        for (int i = 0; i < input.length() - n; i++) {
            int count = 0;
            for (int k = i; k < n; k++) {
                count += input.charAt(k);
                System.out.println(count);
            }
            if (count < mini) {
                mini = Integer.min(mini, count);
            }
        }

        return mini;
    }
    public static void main(String[] args) {

        System.out.println(countocc("0001001011"));



//        int windowSum = 0;
//        int maxSum = 0;
//
//            // first window
//            for (int i = 0; i < n; i++) {
//                windowSum += input.charAt(i);
//            }
//
//            maxSum = windowSum;
//
//            // slide window
//            for (int i = ; i < input.length(); i++) {
//                windowSum += input.charAt(i);       // add next element
//                windowSum -= input.charAt(i - n);   // remove previous element
//
//                maxSum = Math.max(maxSum, windowSum);
//            }
//
//            return maxSum;
//        }




    }


}
