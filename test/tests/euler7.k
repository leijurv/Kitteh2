isPrime(int num) int{
	for j:=3; j*j<=num; j=j+2{
		if num % j==0{
			return 0
		}
	}
	return 1
}
func main() void{
	i:=3
	for soFar:=0; true; i=i+2{
		if isPrime(i)==1{
			soFar++
			if soFar==10000{
				print(i)
				break
			}
		}
	}
}