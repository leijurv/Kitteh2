struct bigint{
	int size
	bool* cont
	int cap
	bool neg
	tostring(byte lastChar)byte*{
		zero:=fromLong(0)
		neg:=this.neg
		this.neg=false
		b:=add(this,zero)
		ten:=fromLong(10)
		copy:=add(b,zero)
		count:=0
		for greater(copy,zero){
			wew:=divide(copy,ten)
			copy.free()
			copy=wew
			count++
		}
		if count==0{
			count++
		}
		if neg{
			count++
		}
		copy.free()
		out:=(byte*)malloc((count+2)*sizeof(byte))
		out[count+1]=0
		out[count]=lastChar
		count--
		for count>=0{
			dm:=dividemod(b,ten)
			b.free()
			b=dm.quotient
			digi:=(dm.remainder).toLong()
			(dm.remainder).free()
			free(dm)
			digit:=(byte)digi+'0'
			out[count]=digit
			count--
		}
		if neg{
			out[0]='-'
		}
		b.free()
		ten.free()
		zero.free()
		this.neg=neg
		return out
	}
	toLong()long{
		long res=0
		for i:=this.size-1; i>=0; i--{
			res=res*2
			if this.cont[i]{
				res++
			}
		}
		if this.neg{
			res=0-res
		}
		return res
	}
	output(){
		out:=this.tostring(10)
		writeNullTerm(out)
		free(out)
	}
	leftshift(int amount){
		origsize:=this.size
		this.size=this.size+amount
		bool* newCont
		bool reallocated=this.cap<this.size
		if reallocated{
			newCont=(bool*)malloc(this.size*sizeof(bool))
			this.cap=this.size
		}else{
			newCont=this.cont
		}
		for i:=origsize-1; i>=0; i--{
			newCont[i+amount]=this.cont[i]
		}
		for i:=0; i<amount; i++{
			newCont[i]=false
		}
		if reallocated{
			free(this.cont)
		}
		this.cont=newCont
	}
	free(){
		free(this.cont)
		this.cont=(bool*)0//make sure we dont have any intermittent use-after-free bugs lurking
	}
	highestbitset()int{
		for i:=this.size-1;i>=0;i--{
			wew:=this.cont[i]
			if wew{
				return i
			}
		}
		return 0-1
	}
	dup()bigint*{
		this.trimInPlace()
		res:=new(this.size)
		for i:=0; i<this.size; i++{
			res.cont[i]=this.cont[i]
		}
		res.neg=this.neg
		return res
	}
	trim()bigint*{
		return this.padtolength(this.highestbitset()+1)
	}
	trimInPlace(){
		this.size=this.highestbitset()+1
		if this.size==0{
			this.neg=false //cant have negative zero
			this.size=1
		}
	}
	padtolength(int length1)bigint*{
		length:=length1
		if length==0{
			length=1
		}
		res:=new(length)
		for i:=0; i<this.size && i<length; i++{
			res.cont[i]=this.cont[i]
		}
		res.neg=this.neg
		return res
	}
	rightshiftone()bigint*{
		this.trimInPlace()
		res:=new(this.size-1)
		for i:=0; i<this.size-1; i++{
			res.cont[i]=this.cont[i+1]
		}
		res.neg=this.neg
		return res
	}
	add(bigint*b)bigint*{
		return add(this,b)
	}
	subtract(bigint*b)bigint*{
		return subtract(this,b)
	}
	multiply(bigint*b)bigint*{
		return multiply(this,b)
	}
	divide(bigint*b)bigint*{
		return divide(this,b)
	}
	mod(bigint*b)bigint*{
		return mod(this,b)
	}
	dividemod(bigint*b)quotremain*{
		return dividemod(this,b)
	}
	modpow(bigint*pow,bigint*modulus)bigint*{
		return modpow(this,pow,modulus)
	}
	modInverse(bigint*modulus)bigint*{
		return modInverse(this,modulus)
	}
}
fromLong(long inp)bigint*{
	neg:=inp<(long)0
	if neg{
		inp=0-inp
	}
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
	x.trimInPlace()
	x.neg=neg
	return x
}
new(int size)bigint*{
	res:=(bigint*)malloc(sizeof(bigint))
	res.size=size
	res.cap=size
	res.cont=(bool*)malloc(sizeof(bool)*size)
	for i:=0; i<size; i++{
		res.cont[i]=false
	}
	res.neg=false
	return res
}
add(bigint* a,bigint* b)bigint*{
	return addWithCin(a,b,false)
}
addWithCin(bigint* a,bigint* b,bool cin)bigint*{
	if a.neg{
		if b.neg{
			a.neg=false
			b.neg=false
			res:=add(a,b)
			res.neg=true
			a.neg=true
			b.neg=true
			return res
		}
		//b-a
		a.neg=false
		res:=b.subtract(a)
		a.neg=true
		return res
	}
	if b.neg{
		//a-b
		b.neg=false
		res:=a.subtract(b)
		b.neg=true
		return res
	}
	size:=a.size
	if b.size>size{
		size=b.size
	}
	size++
	res:=new(size)
	res.cont[0]=cin
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
		res.cont[i]=ai^bi^ci
		res.cont[i+1]=ai&bi|(ai|bi)&ci
	}
	return res
}
subtract(bigint*aa,bigint*bb)bigint*{
	a:=aa
	b:=bb
	if a.neg{
		if b.neg{
			//-a - -b
			//-a + b
			//b-a
			b.neg=false
			a.neg=false
			res:=b.subtract(a)
			b.neg=true
			a.neg=true
			return res
		}
		//-a - b
		//-(a+b)
		a.neg=false
		res:=add(a,b)
		a.neg=true
		res.neg=true
		return res
	}
	if b.neg{
		//a - -b
		//a+b
		b.neg=false
		res:=add(a,b)
		b.neg=true
		return res
	}
	g:=greater(b,a)
	if g{
		res:=b.subtract(a)
		res.neg=true
		return res
	}
	a.trimInPlace()
	b=b.padtolength(a.size)
	for i:=0; i<b.size; i++{
		b.cont[i]=true^b.cont[i]
	}
	res:=addWithCin(a,b,true)
	res.size=a.size
	b.free()
	return res
}
greater(bigint*a,bigint*b)bool{
	if a.neg{
		if b.neg{
			a.neg=false
			b.neg=false
			res:=greater(b,a)
			a.neg=true
			b.neg=true
			return res
		}
		return false
	}
	if b.neg{
		return true
	}
	if a.highestbitset()>b.highestbitset(){
		return true
	}
	if a.highestbitset()<b.highestbitset(){
		return false
	}
	for i:=a.highestbitset(); i>=0; i--{
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
	if a.neg!=b.neg{
		return false
	}
	if a.highestbitset()!=b.highestbitset(){
		return false
	}
	for i:=0; i<a.highestbitset(); i++{
		if a.cont[i]!=b.cont[i]{
			return false
		}
	}
	return true
}

multiply(bigint* a, bigint* bb)bigint*{
	a.trimInPlace()
	bb.trimInPlace()
	b:=bb.padtolength(a.size+bb.size)
	bneg:=bb.neg
	aneg:=a.neg
	a.neg=false
	b.neg=false
	res:=new(1)
	for i:=0; i<a.size; i++{
		ai:=a.cont[i]
		if ai{
			old:=res
			res=add(res,b)
			old.free()
		}
		b.leftshift(1)
	}
	res.neg=aneg!=bneg
	a.neg=aneg
	b.neg=bneg
	b.free()
	return res
}
struct quotremain{
	bigint* quotient
	bigint* remainder
}
dividemod(bigint*a,bigint*b)quotremain*{
	a.trimInPlace()
	bneg:=b.neg
	b.neg=false
	aneg:=a.neg
	a.neg=false
	d:=new(a.size)
	d.size=0
	res:=new(a.size)
	for e:=a.size-1;e>=0;e--{
		d.leftshift(1)
		d.cont[0]=a.cont[e]
		g:=greater(b,d)
		if(!g){
			old:=d
			d=d.subtract(b)
			old.free()
			res.cont[e]=true
		}
	}
	res.trimInPlace()
	d.trimInPlace()
	qr:=(quotremain*)malloc(sizeof(quotremain))
	res.neg=aneg!=bneg
	qr.quotient=res
	d.neg=aneg
	qr.remainder=d
	b.neg=bneg
	a.neg=aneg
	return qr
}
divide(bigint*a,bigint*b)bigint*{
	dm:=dividemod(a,b)
	res:=dm.quotient
	(dm.remainder).free()
	free(dm)
	return res
}
mod(bigint*a,bigint*b)bigint*{
	dm:=dividemod(a,b)
	res:=dm.remainder
	(dm.quotient).free()
	free(dm)
	return res
}
modpow(bigint*base,bigint*pow,bigint*modulus)bigint*{
	if pow.neg || base.neg || modulus.neg{
		return (bigint*)0
	}
	return modpowInternal(base,pow,modulus)
}
modpowInternal(bigint*base,bigint*pow,bigint*modulus)bigint*{
	pow.trimInPlace()
	if pow.size==1 && pow.cont[0]{
		return base.dup()
	}
	if pow.size<2{
		one:=new(1)
		one.cont[0]=true
		return one
	}
	if pow.cont[0]{
		//odd
		one:=new(1)
		one.cont[0]=true
		powminus:=pow.subtract(one)
		(one).free()
		lol:=modpowInternal(base,powminus,modulus)
		(powminus).free()
		lmao:=multiply(base,lol)
		(lol).free()
		res:=mod(lmao,modulus)
		(lmao).free()
		return res
	}
	shiftedPow:=pow.rightshiftone()
	wew:=modpowInternal(base,shiftedPow,modulus)
	shiftedPow.free()
	wewwew:=multiply(wew,wew)
	(wew).free()
	res:=mod(wewwew,modulus)
	res.trimInPlace()
	(wewwew).free()
	return res
}
modInverse(bigint*a,bigint*b)bigint*{
	s:=fromLong(0)
	olds:=fromLong(1)
	t:=fromLong(1)
	oldt:=fromLong(0)
	r:=b.dup()
	oldr:=a.dup()
	zero:=fromLong(0)
	for greater(r,zero){
		quotient:=divide(oldr,r)
		qr:=multiply(quotient,r)
		qs:=multiply(quotient,s)
		qt:=multiply(quotient,t)
		newr:=oldr.subtract(qr)
		news:=olds.subtract(qs)
		newt:=oldt.subtract(qt)
		(quotient).free()
		(qr).free()
		(qs).free()
		(qt).free()
		(oldr).free()
		(olds).free()
		(oldt).free()
		oldr=r
		olds=s
		oldt=t
		r=newr
		s=news
		t=newt
	}
	(zero).free()
	(r).free()
	(s).free()
	(t).free()
	(oldr).free()
	(oldt).free()
	wewlad:=add(olds,b)
	free(olds)
	res:= mod(wewlad,b)
	(wewlad).free()
	return res
}