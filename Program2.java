public class Program2 {
public static void main(String[] fin) {
int[] ARGS = new int[fin.length];
for(int i = 0; i < fin.length; i++){
ARGS[i] = Integer.parseInt(fin[i]);
}
int x = ARGS[0];
int i = 2;
boolean found = false;
while(i < x/2 && !found){
int j = 2;
while(j < x/2 && !found){
if (i*j == x) {
found = true;
}
}
}
if (!found) {
System.out.print("not ");
}
System.out.print("found\n");

}
}
