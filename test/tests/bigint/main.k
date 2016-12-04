main(){
	output(multiply(fromLong(2352),fromLong(234987)))
}
struct bigint{
	int size
	bool* cont
	int cap
}
fromLong(long i)bigint*{
	x:=new(1)
	for i>toLong(x){
		one:=new(1)
		one.cont[0]=true
		x=add(x,one)
		trimInPlace(x)
	}
	return x
}
toLong(bigint* b)long{
	long res=0
	for i:=b.size-1; i>=0; i--{
		res=res*2
		if b.cont[i]{
			res++
		}
	}
	return res
}
output(bigint* b){
	print(toLong(b))
}
new(int size)bigint*{
	res:=(bigint*)malloc(sizeof(bigint))
	res.size=size
	res.cap=size
	res.cont=(bool*)calloc(sizeof(bool)*size)
	return res
}
add(bigint* a,bigint* b)bigint*{
	size:=a.size
	if b.size>size{
		size=b.size
	}
	size++
	res:=new(size)
	for i:=0; i<a.size || i<b.size; i++{
		ai:=false
		if i<a.size{
			ai=a.cont[i]
		}
		bi:=false
		if i<b.size{
			bi=b.cont[i]
		}
		ci:=res.cont[i]
		tm:=(ai||bi)&&(!ai||!bi)
		res.cont[i]=(tm||ci)&&(!tm||!ci)
		res.cont[i+1]=(ai&&bi)||((ai||bi)&&ci)
	}
	return res
}
sub(bigint*aa,bigint*bb)bigint*{
	a:=aa
	b:=bb
	g:=greater(b,a)
	if g{
		return (bigint*)0
	}
	a=trim(a)
	b=padtolength(b,a.size)
	for i:=0; i<b.size; i++{
		bi:=b.cont[i]
		if bi{
			b.cont[i]=false
		}else{
			b.cont[i]=true
		}
	}
	one:=new(1)
	one.cont[0]=true
	bplusone:=add(b,one)
	res:=add(a,bplusone)
	res.size=a.size
	f(a)
	f(b)
	f(one)
	f(bplusone)
	return res
}
greater(bigint*a,bigint*b)bool{
	if highestbitset(a)>highestbitset(b){
		return true
	}
	if highestbitset(a)<highestbitset(b){
		return false
	}
	for i:=highestbitset(a); i>=0; i--{
		ai:=a.cont[i]
		bi:=b.cont[i]
		if ai && !bi{
			return true
		}
		if !ai && bi{
			return false
		}
	}
	return false
}
equal(bigint*a,bigint*b)bool{
	if highestbitset(a)!=highestbitset(b){
		return false
	}
	for i:=0; i<highestbitset(a); i++{
		if a.cont[i]!=b.cont[i]{
			return false
		}
	}
	return true
}
highestbitset(bigint*a)int{
	for i:=a.size-1;i>=0;i--{
		wew:=a.cont[i]
		if wew{
			return i
		}
	}
	return 0-1
}
trim(bigint* a)bigint*{
	return padtolength(a,highestbitset(a)+1)
}
trimInPlace(bigint* a){
	a.size=highestbitset(a)+1
}
padtolength(bigint*a,int length)bigint*{
	res:=new(length)
	for i:=0; i<a.size; i++{
		res.cont[i]=a.cont[i]
	}
	return res
}
leftshift(bigint*a,int amount){
	origsize:=a.size
	a.size=a.size+amount
	bool* newCont
	bool reallocated=!(a.cap>=a.size)
	if reallocated{
		newCont=(bool*)calloc(a.size*sizeof(bool))
		a.cap=a.size
	}else{
		newCont=a.cont
	}
	for i:=origsize-1; i>=0; i--{
		newCont[i+amount]=a.cont[i]
	}
	if !reallocated{//only clear if we didn't calloc beacuse calloc clears for us
		for i:=0; i<amount; i++{
			newCont[i]=false
		}
	}else{
		free(a.cont)
	}
	a.cont=newCont
}
multiply(bigint* a, bigint* bb)bigint*{
	b:=trim(bb)
	res:=new(1)
	for i:=0; i<a.size; i++{
		ai:=a.cont[i]
		if ai{
			old:=res
			res=add(res,b)
			f(old)
		}
		leftshift(b,1)
	}
	f(b)
	return res
}
struct quotremain{
	bigint* quotient
	bigint* remainder
}
f(bigint*a){
	free(a.cont)
	free(a)
}
dividemod(bigint*a,bigint*b)quotremain*{
	d:=new(0)
	res:=new(a.size)
	for e:=a.size-1;e>=0;e--{
		leftshift(d,1)
		d.cont[0]=a.cont[e]
		g:=greater(b,d)
		if(!g){
			old:=d
			d=sub(d,b)
			f(old)
			res.cont[e]=true
		}
	}
	trimInPlace(res)
	trimInPlace(d)
	qr:=(quotremain*)malloc(sizeof(quotremain))
	qr.quotient=res
	qr.remainder=d
	return qr
}
divide(bigint*a,bigint*b)bigint*{
	dm:=dividemod(a,b)
	res:=dm.quotient
	f(dm.remainder)
	free(dm)
	return res
}
mod(bigint*a,bigint*b)bigint*{
	dm:=dividemod(a,b)
	res:=dm.remainder
	f(dm.quotient)
	free(dm)
	return res
}