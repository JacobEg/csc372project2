public class Program1 {
public static void main(String[] fin) {
int[] ARGS = new int[fin.length];
for(int i = 0; i < fin.length; i++){
ARGS[i] = Integer.parseInt(fin[i]);
}
int x = ARGS[0];
int y = ARGS[1];
int m = ARGS[2];
int count = 0;
int i = 0;
while(i <= m){
if (i % x == 0 || i % y == 0) {
count = count + 1;
}
i = i + 1;
}
System.out.print(count);
System.out.print("\n");

}
}
