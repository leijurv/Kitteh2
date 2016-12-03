func isPrime(int num) int{
	for j:=3; j*j≤num; j=j+2{
		if num % j==0{
			return 0
		}
	}
	return 1
}
func main(){
	int count=1
	for i:=3; i<1000000; i=i+2{
		if isPrime(i)≠0{
			count=count+1
		}
	}
	print(count)
}