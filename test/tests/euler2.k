main(){
	prev:=1
	this:=1
	sum:=0
	for this < 4000000{
		if this%2==0{
			sum=sum+this
		}
		wew:=this
		this=prev+this
		prev=wew
	}
	print(sum)
}
