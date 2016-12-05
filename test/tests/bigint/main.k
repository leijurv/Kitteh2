main(){
	output(multiply(fromLong(2352),fromLong(234987)))
}
struct bigint{
	int size
	bool* cont
	int cap
}
fromLong(long inp)bigint*{
	count:=0
	copy:=inp
	for copy>(long)0{
		copy=copy/2
		count++
	}
	x:=new(count+1)
	for i:=0; inp>(long)0; i++{
		x.cont[i]=inp%(long)2==(long)1
		inp=inp/2
	}
	trimInPlace(x)
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
output(bigint* bbb){
	zero:=fromLong(0)
	b:=add(bbb,zero)
	ten:=fromLong(10)
	copy:=add(b,zero)
	count:=0
	for greater(copy,zero){
		wew:=divide(copy,ten)
		f(copy)
		copy=wew
		count++
	}
	f(copy)
	out:=(byte*)malloc((count+2)*sizeof(byte))
	out[count+1]=0
	out[count]=10
	count--
	for greater(b,zero){
		dm:=dividemod(b,ten)
		f(b)
		b=dm.quotient
		digi:=toLong(dm.remainder)
		f(dm.remainder)
		free(dm)
		digit:=(byte)digi+'0'
		out[count]=digit
		count--
	}
	f(b)
	f(ten)
	f(zero)
	print(out)
	free(out)
}
new(int size)bigint*{
	res:=(bigint*)malloc(sizeof(bigint))
	res.size=size
	res.cap=size
	res.cont=(bool*)malloc(sizeof(bool)*size)
	for i:=0; i<size; i++{
		res.cont[i]=false
	}
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
	for i:=0; i<a.size && i<length; i++{
		res.cont[i]=a.cont[i]
	}
	return res
}
leftshift(bigint*a,int amount){
	origsize:=a.size
	a.size=a.size+amount
	bool* newCont
	bool reallocated=a.cap<a.size
	if reallocated{
		newCont=(bool*)malloc(a.size*sizeof(bool))
		a.cap=a.size
	}else{
		newCont=a.cont
	}
	for i:=origsize-1; i>=0; i--{
		newCont[i+amount]=a.cont[i]
	}
	for i:=0; i<amount; i++{
		newCont[i]=false
	}
	if reallocated{
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
	trimInPlace(a)
	d:=new(a.size)
	d.size=0
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
dup(bigint* a)bigint*{
	trimInPlace(a)
	res:=new(a.size)
	for i:=0; i<a.size; i++{
		res.cont[i]=a.cont[i]
	}
	return res
}
rightshiftone(bigint*a)bigint*{
	trimInPlace(a)
	res:=new(a.size-1)
	for i:=0; i<a.size-1; i++{
		res.cont[i]=a.cont[i+1]
	}
	return res
}
modpow(bigint*base,bigint*pow,bigint*modulus)bigint*{
	return modpowInternal(dup(base),dup(pow),dup(modulus))
}
modpowInternal(bigint*base,bigint*pow,bigint*modulus)bigint*{
	trimInPlace(pow)
	if pow.size==1{
		return dup(base)
	}
	if pow.size==0{
		one:=new(1)
		one.cont[0]=true
		return one
	}
	if pow.cont[0]{
		//odd
		//pow.cont[0]=false // who needs subtract when you can set the bits manually
		one:=new(1)
		one.cont[0]=true
		powminus:=sub(pow,one)
		f(one)
		lol:=modpowInternal(base,powminus,modulus)
		f(powminus)
		lmao:=multiply(base,lol)
		f(lol)
		res:=mod(lmao,modulus)
		f(lmao)
		return res
	}
	shiftedPow:=rightshiftone(pow)
	wew:=modpowInternal(base,shiftedPow,modulus)
	f(shiftedPow)
	wewwew:=multiply(wew,wew)
	f(wew)
	res:=mod(wewwew,modulus)
	trimInPlace(res)
	f(wewwew)
	return res
}