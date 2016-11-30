/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;
import compiler.tac.optimize.OptimizationSettings;
import compiler.tac.optimize.TACOptimizer;
import compiler.tac.optimize.UselessTempVars;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author leijurv
 */
public class CompilerTest {
    public CompilerTest() {
    }
    @BeforeClass
    public static void setUpClass() {
    }
    @AfterClass
    public static void tearDownClass() {
    }
    @Before
    public void setUp() {
    }
    @After
    public void tearDown() {
    }
    @Test
    public void testSimpleCompile() throws Exception {
        verifyCompilation("func main(){\nprint(5)\nprint(6)\n}", true, "5\n6\n");
        verifyCompilation("func main(){\nprint(5)\n}", true, "5\n");
        verifyCompilation("func main(){\na:=420\nprint(a)\n}", true, "420\n");
        verifyCompilation("func main(){\na:=420\nprint(a+a)\n}", true, "840\n");
        verifyCompilation("func main(){\na:=420\nprint((a-1)*3)\n}", true, "1257\n");
    }
    @Test
    public void testSimpleNonCompile() throws Exception {
        shouldntCompile("func main(){\nprint(5)\nprint6)\n}");
        shouldntCompile("");
        shouldntCompile("func main({}");
        shouldntCompile("func main(){}");
        shouldntCompile("func main(){\n}");
    }
    @Test
    public void testEuler() throws Exception {
        verifyCompilation("func main(){ \n sum:=0 \n for i:=0; i<1000; i=i+1 { \n if i%3==0||i%5==0{ \n sum=sum+i \n } \n } \n print(sum) \n }", true, "233168\n");
    }
    @Test
    public void testEuler2() throws Exception {
        verifyCompilation("func main(){\n"
                + "	prev:=1\n"
                + "	this:=1\n"
                + "	sum:=0\n"
                + "	for this < 4000000{\n"
                + "		if this%2==0{\n"
                + "			sum=sum+this\n"
                + "		}\n"
                + "		wew:=this\n"
                + "		this=prev+this\n"
                + "		prev=wew\n"
                + "	}\n"
                + "	print(sum)\n"
                + "}", true, "4613732\n");
    }
    @Test
    public void testEuler6() throws Exception {
        verifyCompilation("func main(){\n"
                + "	long sum=0\n"
                + "	int othersum=0\n"
                + "	for i:=0; i<=100; i++{\n"
                + "		sum=sum+(long)(i*i)\n"
                + "		othersum=othersum+i\n"
                + "	}\n"
                + "	print((long)othersum*(long)othersum-sum)\n"
                + "}", true, "25164150\n");//tests proper handling of longs and casting, as the numbers here are greater than 2^32
    }
    @Test
    public void testEuler7() throws Exception {
        verifyCompilation("func isPrime(int num) int{\n"
                + "	for j:=3; j*j<=num; j=j+2{\n"
                + "		if num % j==0{\n"
                + "			return 0\n"
                + "		}\n"
                + "	}\n"
                + "	return 1\n"
                + "}\n"
                + "func main(){\n"
                + "	i:=3\n"
                + "	for soFar:=0; true; i=i+2{\n"
                + "		if isPrime(i)==1{\n"
                + "			soFar++\n"
                + "			if soFar==10000{\n"
                + "				print(i)\n"
                + "				break\n"
                + "			}\n"
                + "		}\n"
                + "	}\n"
                + "}", true, "104743\n");
    }
    @Test
    public void countPrimes() throws Exception {
        verifyCompilation("func isPrime(int num) int{\n"
                + "	for j:=3; j*j≤num; j=j+2{\n"
                + "		if num % j==0{\n"
                + "			return 0\n"
                + "		}\n"
                + "	}\n"
                + "	return 1\n"
                + "}\n"
                + "func main(){\n"
                + "	int count=1\n"
                + "	for i:=3; i<1000000; i=i+2{\n"
                + "		if isPrime(i)≠0{\n"
                + "			count=count+1\n"
                + "		}\n"
                + "	}\n"
                + "	print(count)\n"
                + "}", true, "78498\n");
    }
    @Test
    public void testVariousStructs() throws Exception {
        verifyCompilation("struct teststruct{\n"
                + "	int a;\n"
                + ";\n"
                + "	int b//lmao\n"
                + "	long* c ;; ;; ;; ;; ; ; ; ; ; ; ; ;  ;\n"
                + "}\n"
                + "func main(){\n"
                + "	b:=(teststruct*)malloc(420)\n"
                + "	teststruct* c =b\n"
                + "	((int*)b)[0]=420\n"
                + "	((int*)b)[1]=5021\n"
                + "	ptr:=(long*)malloc(5021)\n"
                + "	ptr[0]=420420420\n"
                + "	((long**)b)[1]=ptr // [1] because long*s are 8 bytes so [1] lines up with the beginning of c in teststruct\n"
                + "	print(c[0].a)\n"
                + "	c[0].a=5\n"
                + "	print(c[0].a)\n"
                + "	print(c[0].b)\n"
                + "	print(c[0].c[0])\n"
                + "	*ptr=*ptr+50215021\n"
                + "	print(c[0].c[0])\n"
                + "	teststruct aoeu=*c\n"
                + "	print(aoeu.a)\n"
                + "	aoeu.a=aoeu.a+46290\n"
                + "	print(aoeu.a)\n"
                + "	aoeu.b=444\n"
                + "	print(aoeu.a)\n"
                + "	print(aoeu.b)\n"
                + "	print((*c).a)\n"
                + "}", true, "420\n5\n5021\n420420420\n470635441\n5\n46295\n46295\n444\n5\n");
    }
    @Test
    public void testLinkedList() throws Exception {
        String[] structDefinitionVariants = {"struct linked{\n"//ensure that the position of fields in the struct doesn't affect execution
            + "	long this\n"
            + "	linked* next\n"
            + "	bool hasNext\n"
            + "}", "struct linked{\n"
            + "	long this\n"
            + "	bool hasNext\n"
            + "	linked* next\n"
            + "}", "struct linked{\n"
            + "	bool hasNext\n"
            + "	long this\n"
            + "	linked* next\n"
            + "}", "struct linked{\n"
            + "	bool hasNext\n"
            + "	linked* next\n"
            + "	long this\n"
            + "}", "struct linked{\n"
            + "	linked* next\n"
            + "	bool hasNext\n"
            + "	long this\n"
            + "}", "struct linked{\n"
            + "	linked* next\n"
            + "	long this\n"
            + "	bool hasNext\n"
            + "}"};
        String cont = "func main(){\n"
                + "	ll:=newLinked(1)\n"
                + "	for long i=1; i<(long)30; i=i+1{\n"
                + "		ll=add(factorial(i),ll)\n"
                + "	}\n"
                + "	pll(ll)\n"
                + "}\n"
                + "func newLinked(long val) linked*{\n"
                + "	linked* root=(linked*)malloc(17)\n"
                + "	root[0].this=val\n"
                + "	root[0].hasNext=false\n"
                + "	return root\n"
                + "}\n"
                + "func add(long i, linked* ptr) linked*{\n"
                + "	newRoot:=newLinked(i)\n"
                + "	newRoot[0].hasNext=true\n"
                + "	newRoot[0].next=ptr\n"
                + "	return newRoot\n"
                + "}\n"
                + "func pll(linked* ptr){\n"
                + "	print((*ptr).this)\n"
                + "	if (*ptr).hasNext {\n"
                + "		pll((*ptr).next)\n"
                + "	}\n"
                + "}\n"
                + "func factorial(long i) long{\n"
                + "	if i≥(long)1{\n"
                + "		return i*factorial(i-1)\n"
                + "	}\n"
                + "	return 1\n"
                + "}";
        String result = "-7055958792655077376\n"
                + "-5968160532966932480\n"
                + "-5483646897237262336\n"
                + "-1569523520172457984\n"
                + "7034535277573963776\n"
                + "-7835185981329244160\n"
                + "8128291617894825984\n"
                + "-1250660718674968576\n"
                + "-4249290049419214848\n"
                + "2432902008176640000\n"
                + "121645100408832000\n"
                + "6402373705728000\n"
                + "355687428096000\n"
                + "20922789888000\n"
                + "1307674368000\n"
                + "87178291200\n"
                + "6227020800\n"
                + "479001600\n"
                + "39916800\n"
                + "3628800\n"
                + "362880\n"
                + "40320\n"
                + "5040\n"
                + "720\n"
                + "120\n"
                + "24\n"
                + "6\n"
                + "2\n"
                + "1\n"
                + "1\n";
        String intVersionResult = "-1241513984\n"
                + "-1375731712\n"
                + "1484783616\n"
                + "-1853882368\n"
                + "2076180480\n"
                + "-775946240\n"
                + "862453760\n"
                + "-522715136\n"
                + "-1195114496\n"
                + "-2102132736\n"
                + "109641728\n"
                + "-898433024\n"
                + "-288522240\n"
                + "2004189184\n"
                + "2004310016\n"
                + "1278945280\n"
                + "1932053504\n"
                + "479001600\n"
                + "39916800\n"
                + "3628800\n"
                + "362880\n"
                + "40320\n"
                + "5040\n"
                + "720\n"
                + "120\n"
                + "24\n"
                + "6\n"
                + "2\n"
                + "1\n"
                + "1\n";
        for (String a : structDefinitionVariants) {
            String program = a + "\n" + cont;
            verifyCompilation(program, true, result);
            program = program.replace("(long)", "");
            program = program.replace("long", "int");
            verifyCompilation(program, true, intVersionResult);
        }
    }
    @Test
    public void testWolvesAndSheep() throws Exception {
        verifyCompilation("func main(){\n"
                + "	wolves:=newBoard(5)\n"
                + "	attacked:=newBoard(5)\n"
                + "	seed:=(int*)malloc(sizeof(int))\n"
                + "	*seed=1\n"
                + "	for true{\n"
                + "		for x:=0; x<5; x++{\n"
                + "			for y:=0; y<5; y++{\n"
                + "				wolves[x][y]=0\n"
                + "				attacked[x][y]=0\n"
                + "			}\n"
                + "		}\n"
                + "		for i:=0; i<5; i++{\n"
                + "			x:=0\n"
                + "			y:=0\n"
                + "			for true{\n"
                + "				x=r(seed,5)\n"
                + "				y=r(seed,5)\n"
                + "				if wolves[x][y]==0{\n"
                + "					break\n"
                + "				}\n"
                + "			}\n"
                + "			wolves[x][y]=1\n"
                + "			for dirX:=0-1; dirX<=1; dirX++{\n"
                + "				for dirY:=0-1; dirY<=1; dirY++{\n"
                + "					if dirX==0 && dirY==0{\n"
                + "						continue\n"
                + "					}\n"
                + "					fillDirection(attacked,x,y,dirX,dirY)\n"
                + "				}\n"
                + "			}\n"
                + "		}\n"
                + "		open:=0\n"
                + "		for x:=0; x<5; x++{\n"
                + "			for y:=0; y<5; y++{\n"
                + "				if attacked[x][y]==0{\n"
                + "					open++\n"
                + "				}\n"
                + "			}\n"
                + "		}\n"
                + "		if open>=3{\n"
                + "			for x:=0; x<5; x++{\n"
                + "				for y:=0; y<5; y++{\n"
                + "					if wolves[x][y]==1{\n"
                + "						pr('W')\n"
                + "						continue\n"
                + "					}\n"
                + "					if attacked[x][y]==1{\n"
                + "						pr('+')\n"
                + "						continue\n"
                + "					}\n"
                + "					pr('S')\n"
                + "				}\n"
                + "				pr(10)\n"
                + "			}\n"
                + "			print(*seed)\n"
                + "			break\n"
                + "		}\n"
                + "	}\n"
                + "}\n"
                + "func fillDirection(int** board,int x,int y,int dirX,int dirY){\n"
                + "	for x>=0 && y>=0 && x<5 && y<5{\n"
                + "		board[x][y]=1\n"
                + "		x=x+dirX\n"
                + "		y=y+dirY\n"
                + "	}\n"
                + "}\n"
                + "func pr(byte b){\n"
                + "	tmp:=(byte*)malloc(2)\n"
                + "	tmp[0]=b\n"
                + "	tmp[1]=0\n"
                + "	print(tmp)\n"
                + "}\n"
                + "func r(int* seed,int max)int{\n"
                + "	*seed=(*seed*5023+257)%2147483647\n"
                + "	return *seed%max\n"
                + "}\n"
                + "func newBoard(int size) int**{\n"
                + "	board:=(int**)malloc(size*sizeof(int*))\n"
                + "	for i:=0; i<size; i++{\n"
                + "		board[i]=(int*)malloc(size*sizeof(int))\n"
                + "	}\n"
                + "	return board\n"
                + "}", true,
                "+S++S\n"
                + "++++S\n"
                + "W++++\n"
                + "W++W+\n"
                + "++WW+\n"
                + "547485415\n");
    }
    @Test
    public void testFactorialPrint() throws Exception {
        verifyCompilation("func main(){\n"
                + "	for long i=0; i<(long)50; i=i+1{\n"
                + "		println(factorial(i))\n"
                + "	}\n"
                + "}\n"
                + "func println(long i){\n"
                + "	print(intToStr(i))\n"
                + "	byte* nl=(byte*)((malloc(2)))\n"
                + "	nl[0]=10//  aka '\\n' but i don't have escape characters yet\n"
                + "	nl[1]=0//gotta have the null terminator\n"
                + "	print(nl)\n"
                + "}\n"
                + "func intToStr(long input) byte*{\n"
                + "	inputCopy:=input\n"
                + "	int count=0\n"
                + "	if (long)(0) >inputCopy{\n"
                + "		inputCopy=0-inputCopy//make it positive so \"for inputCopy > 0\" works properly\n"
                + "		count=count+1//make room for the negative sign\n"
                + "	}\n"
                + "	for inputCopy > (long)0{\n"
                + "		inputCopy = inputCopy / 10\n"
                + "		count=count+1\n"
                + "	}\n"
                + "	if count == 0{//even if the input is just zero, the output needs to have the \"0\" char\n"
                + "		count=1\n"
                + "	}\n"
                + "	result:=(byte*)malloc(count+1)//don't forget the null pointer at the end\n"
                + "	result[count]=0//set the null pointer\n"
                + "	count--//start at the last char before the null pointer\n"
                + "	if input<(long)0{//if we are doing a negative number\n"
                + "		input=0-input//make it positive\n"
                + "		result[0]='-'//but add the minus sign to the beginning of the output\n"
                + "	}\n"
                + "	for input > (long)0{\n"
                + "		dig:=(byte)(input%(long)10)//the digit as a byte from 0 to 9\n"
                + "		dig=dig+'0'//make it a real ascii character by adding '0' to it\n"
                + "		result[count]=dig//set it in the output\n"
                + "		input=input/10\n"
                + "		count=count-1\n"
                + "	}\n"
                + "	return result\n"
                + "}\n"
                + "func factorial(long i) long{\n"
                + "	wew:=i<=(long)1\n"
                + "otherthingy:=wew\n"
                + "	if otherthingy{\n"
                + "		return 1\n"
                + "	}\n"
                + "	return i*factorial(i-1)\n"
                + "}", true, "1\n"
                + "1\n"
                + "2\n"
                + "6\n"
                + "24\n"
                + "120\n"
                + "720\n"
                + "5040\n"
                + "40320\n"
                + "362880\n"
                + "3628800\n"
                + "39916800\n"
                + "479001600\n"
                + "6227020800\n"
                + "87178291200\n"
                + "1307674368000\n"
                + "20922789888000\n"
                + "355687428096000\n"
                + "6402373705728000\n"
                + "121645100408832000\n"
                + "2432902008176640000\n"
                + "-4249290049419214848\n"
                + "-1250660718674968576\n"
                + "8128291617894825984\n"
                + "-7835185981329244160\n"
                + "7034535277573963776\n"
                + "-1569523520172457984\n"
                + "-5483646897237262336\n"
                + "-5968160532966932480\n"
                + "-7055958792655077376\n"
                + "-8764578968847253504\n"
                + "4999213071378415616\n"
                + "-6045878379276664832\n"
                + "3400198294675128320\n"
                + "4926277576697053184\n"
                + "6399018521010896896\n"
                + "9003737871877668864\n"
                + "1096907932701818880\n"
                + "4789013295250014208\n"
                + "2304077777655037952\n"
                + "-70609262346240000\n"
                + "-2894979756195840000\n"
                + "7538058755741581312\n"
                + "-7904866829883932672\n"
                + "2673996885588443136\n"
                + "-8797348664486920192\n"
                + "1150331055211806720\n"
                + "-1274672626173739008\n"
                + "-5844053835210817536\n"
                + "8789267254022766592\n");
    }
    @Test
    public void testRSA() throws Exception {
        verifyCompilation("func mp(long base,long exp,long mod) long{\n"
                + "	if exp==(long)0{\n"
                + "		return 1\n"
                + "	}\n"
                + "	if exp==(long)1{\n"
                + "		return base\n"
                + "	}\n"
                + "	if exp%(long)2==(long)0{\n"
                + "		a:=mp(base,exp/2,mod)\n"
                + "		return (a*a)%mod\n"
                + "	}\n"
                + "	return (base*mp(base,exp-1,mod))%mod\n"
                + "}\n"
                + "func inv(long e, long t) long{\n"
                + "	for long d=1; d<t; d++{\n"
                + "		if (e*d)%t==(long)1{\n"
                + "			return d\n"
                + "		}\n"
                + "	}\n"
                + "	return 0-1\n"
                + "}\n"
                + "func coprime(long e,long t) bool{\n"
                + "	for long d=2; d<=e; d++{\n"
                + "		if e%d==(long)0 && t%d==(long)0{\n"
                + "			return false\n"
                + "		}\n"
                + "	}\n"
                + "	return true\n"
                + "}\n"
                + "func verify(long e,long d,long n){\n"
                + "	for long i=1; i<n; i++{\n"
                + "		if i≠mp(mp(i,e,n),d,n){\n"
                + "			print(i)\n"
                + "			print(mp(i,e,n))\n"
                + "			print(mp(mp(i,e,n),d,n))\n"
                + "		}\n"
                + "		if i≠mp(mp(i,d,n),e,n){\n"
                + "			print(i)\n"
                + "			print(mp(i,d,n))\n"
                + "			print(mp(mp(i,d,n),e,n))\n"
                + "		}\n"
                + "	}\n"
                + "}\n"
                + "func main(){\n"
                + "	print(mp(93845,24897,3460987))\n"
                + "	verify(17,2753,3233)\n"
                + "	long t=3120\n"
                + "	for long e=2; e<(long)30; e++{\n"
                + "		cop:= coprime(e,t)\n"
                + "		if cop{\n"
                + "			print(e)\n"
                + "			d:=inv(e,t)\n"
                + "			print(d)\n"
                + "			verify(e,d,3233)\n"
                + "		}	\n"
                + "	}\n"
                + "	\n"
                + "}", true, "140025\n"
                + "7\n"
                + "1783\n"
                + "11\n"
                + "851\n"
                + "17\n"
                + "2753\n"
                + "19\n"
                + "2299\n"
                + "23\n"
                + "407\n"
                + "29\n"
                + "1829\n");
    }
    @Test
    public void testOverwriting() throws Exception {
        String header = "func main(){\nprint(test(1,2,3,4))\n}\nfunc test(int a,int b,int c,int j)int{\n";
        String footer = "	return a + (b + c * j + a) + j * (c * j)\n"
                + "}";
        String[] body = {"	print( b + c * j + a)\n",
            "	print(a+b)\n",
            "	print(c*j)\n"};
        String[] outputs = {"15\n", "3\n", "12\n"};
        for (int a = 0; a <= 1; a++) {
            for (int b = 0; b <= 1; b++) {
                for (int c = 0; c <= 1; c++) {
                    String program = header + (a == 1 ? body[0] : "") + (b == 1 ? body[1] : "") + (c == 1 ? body[2] : "") + footer;
                    String out = (a == 1 ? outputs[0] : "") + (b == 1 ? outputs[1] : "") + (c == 1 ? outputs[2] : "") + "64\n";
                    verifyCompilation(program, true, out);
                }
            }
        }
    }
    @Test
    public void tsetLinkedSort() throws Exception {
        verifyCompilation("struct node{\n"
                + "	bool hasNext\n"
                + "	long value\n"
                + "	node* next\n"
                + "}\n"
                + "struct linked{\n"
                + "	node* first\n"
                + "	bool hasFirst\n"
                + "}\n"
                + "func newLinked() linked*{\n"
                + "	res:=(linked*)malloc(sizeof(linked))\n"
                + "	res.hasFirst=false\n"
                + "	return res\n"
                + "}\n"
                + "func add(linked* list, long toAdd){\n"
                + "	n:=(node*)malloc(sizeof(node))\n"
                + "	n.value=toAdd\n"
                + "	if !list.hasFirst{\n"
                + "		n.hasNext=false\n"
                + "		list.hasFirst=true\n"
                + "		list.first=n\n"
                + "		return\n"
                + "	}\n"
                + "	n.hasNext=true\n"
                + "	n.next=list.first\n"
                + "	list.first=n\n"
                + "}\n"
                + "func removeLowest(linked* list) long{\n"
                + "	firstList:=list.first\n"
                + "	this:=list.first\n"
                + "	prev:=(node*)0\n"
                + "	bestPrev:=prev\n"
                + "	lowestValue:=this.value\n"
                + "	for this.hasNext{\n"
                + "		prev=this\n"
                + "		this=this.next\n"
                + "		if this.value < lowestValue{\n"
                + "			lowestValue=this.value\n"
                + "			bestPrev=prev\n"
                + "		}\n"
                + "	}\n"
                + "	if (long)bestPrev==(long)0{\n"
                + "		list.hasFirst=firstList.hasNext\n"
                + "		list.first=firstList.next\n"
                + "		return lowestValue\n"
                + "	}\n"
                + "	toRemove:=bestPrev.next\n"
                + "	bestPrev.next=toRemove.next\n"
                + "	bestPrev.hasNext=toRemove.hasNext\n"
                + "	return lowestValue\n"
                + "}\n"
                + "func sort(long* ptr,int size) {\n"
                + "	l:=newLinked()\n"
                + "	for i:=0; i<size; i++{\n"
                + "		add(l,ptr[i])\n"
                + "	}\n"
                + "	for i:=0; i<size; i++{\n"
                + "		ptr[i]=removeLowest(l)\n"
                + "	}\n"
                + "}\n"
                + "func r(int* seed,int max)int{\n"
                + "	*seed=(*seed*5023+257)%2147483647\n"
                + "	return *seed%max\n"
                + "}\n"
                + "func main(){\n"
                + "	seed:=(int*)malloc(sizeof(int))\n"
                + "	*seed=1234\n"
                + "	num:=10\n"
                + "	toSort:=(long*)malloc(num*sizeof(long))\n"
                + "	for i:=0; i<num; i++{\n"
                + "		toSort[i]=(long)r(seed,100)\n"
                + "	}\n"
                + "	for i:=0; i<num; i++{\n"
                + "		print(toSort[i])\n"
                + "	}\n"
                + "	sort(toSort,num)\n"
                + "	for i:=0; i<num; i++{\n"
                + "		print(toSort[i])\n"
                + "	}\n"
                + "}", true, "39\n"
                + "82\n"
                + "4\n"
                + "53\n"
                + "41\n"
                + "56\n"
                + "29\n"
                + "21\n"
                + "28\n"
                + "14\n"
                + "4\n"
                + "14\n"
                + "21\n"
                + "28\n"
                + "29\n"
                + "39\n"
                + "41\n"
                + "53\n"
                + "56\n"
                + "82\n");
    }
    public void shouldntCompile(String program) throws IOException, InterruptedException {
        verifyCompilation(program, false, null);
    }
    public void verifyCompilation(String program, boolean shouldCompile, String desiredExecutionOutput) throws IOException, InterruptedException {
        try {
            //first check with all optimizations
            //if it works with correct output with all optimizations, then we are gud
            verifyCompilation(program, shouldCompile, desiredExecutionOutput, OptimizationSettings.ALL, false);
        } catch (Exception e) {
            verifyCompilation(program, shouldCompile, desiredExecutionOutput, OptimizationSettings.NONE, true);
            //don't try/catch the no-optimization, because if that fails then that's the error we want to throw
            if (!shouldCompile) {
                return;
                //if it shouldn't compile, and the test was successful (i e it actually didn't compile)
                //we don't need to go on to check other things, it failed without even applying any optimizations
            }
            //ok so it works with none
            detective(program, desiredExecutionOutput, e);
            e.printStackTrace();
            throw new IllegalStateException("Detective failed" + e);//shouldn't get to here
        }
    }
    public Object detective(String program, String desiredExecutionOutput, Exception withAll) {//setting the return type to non-void ensures that it cannot exit without throwing SOME exception
        //no exception with false,NONE
        //exception with true,ALL
        try {
            verifyCompilation(program, true, desiredExecutionOutput, new OptimizationSettings(false, true), false);
        } catch (Exception e) {
            //exception isn't caused by any optimization settings
            e.printStackTrace();
            throw new IllegalStateException("Exception caused by setting staticValues to true with optimizationsettings staying at NONE " + e);
        }
        //no exception with *,NONE
        //try enabling individual optimizations
        for (int i = 0; i < TACOptimizer.opt.size(); i++) {
            OptimizationSettings set = new OptimizationSettings(false, true);
            set.setEnabled(i, true);
            try {
                verifyCompilation(program, true, desiredExecutionOutput, set, false);
            } catch (Exception e) {
                //if enabling one on its own can trigger it, let's just throw that
                e.printStackTrace();
                throw new IllegalStateException("Caused by optimization " + i + " " + TACOptimizer.opt.get(i) + " " + e);
            }
        }
        int uselessTemp = TACOptimizer.opt.indexOf(UselessTempVars.class);
        for (int i = 0; i < TACOptimizer.opt.size(); i++) {
            if (i == uselessTemp) {
                continue;
            }
            OptimizationSettings set = new OptimizationSettings(false, true);
            set.setEnabled(i, true);
            set.setEnabled(uselessTemp, true);
            try {
                verifyCompilation(program, true, desiredExecutionOutput, set, false);
            } catch (Exception e) {
                //if enabling one with uselesstempvars can trigger it, let's just throw that
                e.printStackTrace();
                throw new IllegalStateException("Caused by uselesstempvars AND optimization " + i + " " + TACOptimizer.opt.get(i) + " " + e);
            }
        }
        withAll.printStackTrace();
        throw new IllegalStateException("Exception caused when all are enabled, but not when any are enabled individually, alone or with uselesstempvars" + withAll);
    }
    public void verifyCompilation(String program, boolean shouldCompile, String desiredExecutionOutput, OptimizationSettings settings, boolean useAssert) throws IOException, InterruptedException {
        if (!new File("/usr/bin/gcc").exists()) {
            assertNull("GCC must exist");
        }
        if (!shouldCompile) {
            assertNull(desiredExecutionOutput);
        }
        String compiled;
        try {
            compiled = Compiler.compile(program, settings);
            assertEquals(true, shouldCompile);
        } catch (Exception e) {
            if (shouldCompile) {
                throw e;
            }
            return;
        }
        assertNotNull(compiled);
        File asm = File.createTempFile("kittehtest" + System.nanoTime() + "_" + program.hashCode(), ".s");
        File executable = new File(asm.getAbsolutePath().replace(".s", ".o"));
        assertEquals(false, executable.exists());
        assertEquals(true, asm.exists());
        System.out.println("Writing to file " + asm);
        try (FileOutputStream out = new FileOutputStream(asm)) {
            out.write(compiled.getBytes());
        }
        assertEquals(true, asm.exists());
        String[] compilationCommand = {"gcc", "-o", executable.getAbsolutePath(), asm.getAbsolutePath()};
        System.out.println(Arrays.asList(compilationCommand));
        Process gcc = new ProcessBuilder(compilationCommand).start();
        if (!gcc.waitFor(10, TimeUnit.SECONDS)) {
            gcc.destroyForcibly();
            assertEquals("GCC timed out????", false, true);
        }
        System.out.println("GCC return value: " + gcc.waitFor());
        if (gcc.waitFor() != 0) {
            int j;
            StringBuilder result = new StringBuilder();
            while ((j = gcc.getErrorStream().read()) >= 0) {
                result.append((char) j);
            }
            while ((j = gcc.getInputStream().read()) >= 0) {
                result.append((char) j);
            }
            System.out.println(result);
            System.out.println("Oh well");
        }
        assertEquals(0, gcc.waitFor());
        assertEquals(true, executable.exists());
        Process ex = new ProcessBuilder(executable.getAbsolutePath()).redirectError(Redirect.INHERIT).start();
        if (!ex.waitFor(2, TimeUnit.SECONDS)) {
            ex.destroyForcibly();
            assertEquals("Subprocess timed out", false, true);
        }
        int j;
        StringBuilder result = new StringBuilder();
        while ((j = ex.getInputStream().read()) >= 0) {
            result.append((char) j);
        }
        System.out.println("Execution output \"" + result + "\"");
        if (useAssert) {
            assertEquals(desiredExecutionOutput, result.toString());
        } else if (!desiredExecutionOutput.equals(result.toString())) {
            throw new IllegalStateException(desiredExecutionOutput + "--" + result.toString());
        }
    }
}
