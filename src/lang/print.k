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
	result:=(byte*)malloc(count)
	count--//start at the last char before the null pointer
	neg:=input<(long)0
	if neg{//if we are doing a negative number
		input=0-input//make it positive
	}
	for count>=0{
		dig:=(byte)(input%(long)10)//the digit as a byte from 0 to 9
		dig=dig+'0'//make it a real ascii character by adding '0' to it
		result[count]=dig//set it in the output
		input=input/10
		count=count-1
	}
	if neg{
		result[0]='-'//but add the minus sign to the beginning of the output
	}
	write(result,totalSize)
	free(result)
}
print(long input){
	intToStr(input)
	writeByte(10)
}
writeByte(byte b){
	arr:=(byte*)malloc(1)
	arr[0]=b
	write(arr,1)
	free(arr)
}
writefd(int fd,byte* str, int len){
	if MACOSX{
		num:=2<<24+4
		syscall((long)num,fd,str,len)
	}else{
		syscall(1,fd,str,len)
	}
}
write(byte* buf, int num){
	writefd(1,buf,num)
}
readfd(int fd, byte* buf, int len){
	if MACOSX{
		num:=2<<24+3
		syscall((long)num,fd,buf,len)
	}else{
		syscall(0,fd,buf,len)
	}
}
read(byte* buf, int num){
	readfd(0,buf,num)
}
writeNullTerm(byte* str){
	i:=0
	for copy:=str; *copy!=(byte)0; i++{
		copy=copy+1
	}
	write(str,i)
}