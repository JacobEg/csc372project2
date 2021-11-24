public class TestEx {
public static void main(String[] fin) {
int[] ARGS = new int[fin.length];
for(int i = 0; i < fin.length; i++){
ARGS[i] = Integer.parseInt(fin[i]);
}
int sum = 5 + 3;
boolean q = true && !(sum < 9);
System.out.print(q);

}
}
