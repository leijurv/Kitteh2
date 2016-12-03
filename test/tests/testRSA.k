func mp(long base,long exp,long mod) long{
	if exp==(long)0{
		return 1
	}
	if exp==(long)1{
		return base
	}
	if exp%(long)2==(long)0{
		a:=mp(base,exp/2,mod)
		return (a*a)%mod
	}
	return (base*mp(base,exp-1,mod))%mod
}
func inv(long e, long t) long{
	for long d=1; d<t; d++{
		if (e*d)%t==(long)1{
			return d
		}
	}
	return 0-1
}
func coprime(long e,long t) bool{
	for long d=2; d<=e; d++{
		if e%d==(long)0 && t%d==(long)0{
			return false
		}
	}
	return true
}
func verify(long e,long d,long n){
	for long i=1; i<n; i++{
		if i≠mp(mp(i,e,n),d,n){
			print(i)
			print(mp(i,e,n))
			print(mp(mp(i,e,n),d,n))
		}
		if i≠mp(mp(i,d,n),e,n){
			print(i)
			print(mp(i,d,n))
			print(mp(mp(i,d,n),e,n))
		}
	}
}
func main(){
	print(mp(93845,24897,3460987))
	verify(17,2753,3233)
	long t=3120
	for long e=2; e<(long)30; e++{
		cop:= coprime(e,t)
		if cop{
			print(e)
			d:=inv(e,t)
			print(d)
			verify(e,d,3233)
		}	
	}
	
}