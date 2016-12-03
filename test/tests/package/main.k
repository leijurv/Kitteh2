import ../mathtest/math wewlad
import ../mathtest/main othermain
main(){
	print(fact(5))
	print(wewlad::fact(5))
	print(main::fact(5))
	print(othermain::fact(5))
}
func fact(int k) int{
	return wewlad::fact(k+1)
}