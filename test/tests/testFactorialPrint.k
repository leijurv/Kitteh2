main(){
	for long i=0; i<(long)50; i=i+1{
		println(factorial(i))
	}
}
println(long i){
	intToStr(i)
	byte* nl=(byte*)((malloc(2)))
	nl[0]=10//  aka '\n' but i don't have escape characters yet
	nl[1]=0//gotta have the null terminator
	print(nl)
}
intToStr(long input){
	inputCopy:=input
	int count=0
	if (long)(0) >inputCopy{
		inputCopy=0-inputCopy//make it positive so "for inputCopy > 0" works properly
		count=count+1//make room for the negative sign
	}
	for inputCopy > (long)0{
		inputCopy = inputCopy / 10
		count=count+1
	}
	if count == 0{//even if the input is just zero, the output needs to have the "0" char
		count=1
	}
	totalSize:=count
	result:=(byte*)malloc(count+1)//don't forget the null pointer at the end
	result[count]=0//set the null pointer
	count--//start at the last char before the null pointer
	if input<(long)0{//if we are doing a negative number
		input=0-input//make it positive
		result[0]='-'//but add the minus sign to the beginning of the output
	}
	for input > (long)0{
		dig:=(byte)(input%(long)10)//the digit as a byte from 0 to 9
		dig=dig+'0'//make it a real ascii character by adding '0' to it
		result[count]=dig//set it in the output
		input=input/10
		count=count-1
	}
	print(totalSize)
	print(result)
}
factorial(long i) long{
	wew:=i<=(long)1
	otherthingy:=wew
	if otherthingy{
		return 1
	}else{
		return i*factorial(i-1)
	}
	return 0-5021
}