struct bigint{
	int size
	int* mag
	int cap
	bool neg

debug(){
		print("Outputting")
		writeByte(10)
		print(this.cap)
		for i:=0; i<this.cap; i++{
			print(this.mag[i])
		}
		print("Longval: ")
		print(this.toLong())
		print("End")
		writeByte(10)
	
}
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
			quotient,remain:=dividemod(b,ten)
			b.free()
			b=quotient
			digi:=remain.toLong()
			free(remain)
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
	bit(int pos)bool{
		int n=pos/32
		long k=(long)this.mag[this.cap-n-1]
		long m=1<<((long)pos%32)
		return k&m!=(long)0
	}
	setbit(int pos,bool val){
		int n=pos/32
		int m=1<<(pos%32)
		if val{
			this.mag[this.cap-n-1]=this.mag[this.cap-n-1] | m
		}else{
			this.mag[this.cap-n-1] = this.mag[this.cap-n-1] & (0-1 - m)
		}
	}
	toLong()long{
		long res=0
		for i:=this.size-1; i>=0; i--{
			res=res*2
			if this.bit(i){
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
		this.trimInPlace()
		nInts:=amount>>>5
		nBits:=amount&31
		magLen:=this.cap
		int* n
		nl:=magLen+nInts
		if nBits==0{
			n=(int*)malloc(sizeof(int)*(magLen+nInts))

			for i:=0; i<magLen; i++{
				n[i]=this.mag[i]
			}
			for i:=magLen; i<nl; i++{
				n[i]=0
			}
		}else{
			i:=0
			nBits2:=32-nBits
			highBits:=this.mag[0]>>>nBits2
			if highBits!=0{
				n=(int*)malloc(sizeof(int)*(magLen+nInts+1))
				n[i]=highBits
				i++
				nl++
			}else{
				n=(int*)malloc(sizeof(int)*(magLen+nInts))
			}
			j:=0
			for j < magLen-1{
				n[i]=(this.mag[j]<<nBits)|(this.mag[j+1]>>>nBits2)
				j++
				i++
			}
			n[i]=this.mag[j]<<nBits
		}
		free(this.mag)
		this.mag=n
		this.size=nl*32
		this.cap=nl
		this.trimInPlace()
	}
	free(){
		free(this.mag)
		this.mag=(int*)0//make sure we dont have any intermittent use-after-free bugs lurking
	}
	highestbitset()int{
		for i:=this.size-1;i>=0;i--{
			if this.bit(i){
				return i
			}
		}
		return 0-1
	}
	dup()bigint*{
		this.stripLeadingZero()
		res:=new(this.size)
		for i:=0; i<res.cap; i++{
			res.mag[i]=this.mag[i]
		}
		res.neg=this.neg
		return res
	}
	trim()bigint*{
		return this.padtolength(this.highestbitset()+1)
	}
	trimInPlace(){
		ps:=this.size
		this.size=this.highestbitset()+1
		for i:=ps-1; i>=this.size; i--{
			this.setbit(i,false)
		}
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
			res.setbit(i,this.bit(i))
		}
		res.neg=this.neg
		return res
	}
	stripLeadingZero(){
		this.trimInPlace()
		if this.cap<=1{
			return
		}
		count:=0
		for count < this.cap && this.mag[count]==0{
			count++
		}
		if(count==0){
			return
		}
		n:=(int*)malloc(sizeof(int)*(this.cap-count))
		for i:=this.cap-1; i>=count; i--{
			n[i-count]=this.mag[i]
		}
		free(this.mag)
		this.mag=n
		this.cap=this.cap-count
	}
	rightshiftone()bigint*{
		this.trimInPlace()
		res:=new(this.size-1)
		for i:=0; i<this.size-1; i++{
			res.setbit(i,this.bit(i+1))
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
	dividemod(bigint*b)bigint*,bigint*{
		q,r:=dividemod(this,b)
		return q,r //sorry
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
		x.setbit(i,inp%(long)2==(long)1)
		inp=inp/2
	}
	x.stripLeadingZero()
	x.neg=neg
	return x
}
new(int size)bigint*{
	res:=(bigint*)malloc(sizeof(bigint))
	res.size=size
	s:=size/32
	if size%32!=0{
		s++
	}
	res.cap=s
	res.mag=(int*)malloc(sizeof(int)*s)
	for i:=0; i<s; i++{
		res.mag[i]=0
	}
	res.neg=false
	return res
}
add(bigint* a,bigint* b)bigint*{
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


	 // If x is shorter, swap the two arrays
       if (a.cap < b.cap) {
       	//print("SWITCHEROO")
           bigint* tmp = a;
           a = b;
           b = tmp;
       }
       x:=a.mag
		y:=b.mag
       int xIndex = a.cap;
       int yIndex = b.cap;
       /*print("Caps")
       print(xIndex)
       print(yIndex)*/
       res:=new(xIndex*32)
       result:=res.mag
       
       long sum = 0
       mask:=mask(0)
       // Add common parts of both numbers
       for(yIndex > 0) {
       xIndex--
       yIndex--
       /*print("lol")
       print(mask)
       print(x[xIndex])
       print(y[yIndex])
       print(((long)y[yIndex] & mask))
       print(((long)x[xIndex] & mask))*/
           sum = ((long)x[xIndex] & mask) + ((long)y[yIndex] & mask) + (sum >>> 32);
           //print(sum)
           result[xIndex] = (int)sum;
       }
       // Copy remainder of longer number while carry propagation is required
       bool carry = (sum >>> (long)32 != (long)0);
       for (xIndex > 0 && carry){
       	//print("CARRRYRRYRYRYRRY")
       		xIndex--
       		tmp:=x[xIndex]+1
       		result[xIndex]=tmp
           carry = tmp == 0
       }

       // Copy remainder of longer number
       for (xIndex > 0){
       	//print("COCUCOECI")
       		xIndex--
           result[xIndex] = x[xIndex];
       }

       // Grow result if necessary
       if (carry) {
       	//print("WOW LAD")
           int newLen = res.cap + 1;
           temp:=(int*)malloc(newLen*4)
           for int i = 1; i<newLen; i++{
               temp[i] = result[i-1];
           }
           temp[0] = 1;
           free(result)
           res.mag = temp;
           res.cap=newLen
           res.size=res.size+1
       }
       //writeByte(10)
       //writeByte(10)
       res.trimInPlace()
       return res;
   


	
	
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
	a.stripLeadingZero()
	b.stripLeadingZero()
	g:=greater(b,a)
	if g {
		res:=b.subtract(a)
		res.neg=true
		return res
	}

big:=a.mag
       little:=b.mag
int bigIndex = a.cap;
int littleIndex = b.cap;

	
       res:=new(bigIndex*32)
       result := res.mag
       
     
       
       long difference = 0;
       mask:=mask(0)
       // Subtract common parts of both numbers
       for(littleIndex > 0) {
       bigIndex--
       littleIndex--
           difference = ((long)big[bigIndex] & mask) -((long)little[littleIndex] & mask) +(difference >> 32);
           result[bigIndex] = (int)difference;
       }
       // Subtract remainder of longer number while borrow propagates
       bool borrow = (difference >> (long)32 != (long)0);
       for (bigIndex > 0 && borrow){
       		bigIndex--
       		lol:=big[bigIndex] - 1
       		result[bigIndex]=lol
           borrow = lol == (0-1);
       }
       // Copy remainder of longer number
       for (bigIndex > 0){
       bigIndex--
           result[bigIndex] = big[bigIndex];
       }
res.stripLeadingZero()
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
	ah:=a.highestbitset()
	bh:=b.highestbitset()
	if ah>bh{
		return true
	}
	if ah<bh{
		return false
	}
	for i:=ah; i>=0; i--{
		ai:=a.bit(i)
		bi:=b.bit(i)
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
		if a.bit(i)!=b.bit(i){
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
		ai:=a.bit(i)
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
dividemod(bigint*a,bigint*b)bigint*,bigint*{
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
		d.setbit(0,a.bit(e))
		g:=greater(b,d)
		if(!g){
			old:=d
			d=d.subtract(b)
			old.free()
			res.setbit(e,true)
		}
	}
	res.trimInPlace()
	d.trimInPlace()
	res.neg=aneg!=bneg
	d.neg=aneg
	b.neg=bneg
	a.neg=aneg
	return res,d
}
divide(bigint*a,bigint*b)bigint*{
	quotient,remainder:=dividemod(a,b)
	free(remainder)
	return quotient
}
mod(bigint*a,bigint*b)bigint*{
	quotient,remainder:=dividemod(a,b)
	free(quotient)
	return remainder
}
modpow(bigint*base,bigint*pow,bigint*modulus)bigint*{
	if pow.neg || base.neg || modulus.neg{
		return (bigint*)0
	}
	return modpowInternal(base,pow,modulus)
}
modpowInternal(bigint*base,bigint*pow,bigint*modulus)bigint*{
	pow.trimInPlace()
	if pow.size==1 && pow.bit(0){
		return base.dup()
	}
	if pow.size<2{
		one:=new(1)
		one.setbit(0,true)
		return one
	}
	if pow.bit(0){
		//odd
		one:=new(1)
		one.setbit(0,true)
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
bigintFromHex(byte* str)bigint*{
	count:=0
	for copy:=str; *copy!=(byte)0; copy=copy+1{
		count++
	}
	res:=new(count*4)
	for i:=0; i<count; i++{
		ch:=charFromHex(str[count-i-1])
		res.setbit(0+i*4,ch&(byte)1!=(byte)0)
		res.setbit(1+i*4,ch&(byte)2!=(byte)0)
		res.setbit(2+i*4,ch&(byte)4!=(byte)0)
		res.setbit(3+i*4,ch&(byte)8!=(byte)0)
	}
	return res
}
charFromHex(byte char) byte{
	if char>='0' && char<='9'{
		return char-'0'
	}
	if char>='a' && char<='f'{
		return 10+char-'a'
	}
	if char>='A' && char<='F'{
		return 10+char-'A'
	}
	return char
}
mask(int i) long{
	return (2<<((long)i+31))-1
}