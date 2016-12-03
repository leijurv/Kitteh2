main() void{
	long sum=0
	int othersum=0
	for i:=0; i<=100; i++{
		sum=sum+(long)(i*i)
		othersum=othersum+i
	}
	print((long)othersum*(long)othersum-sum)
}
