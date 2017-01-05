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
		b:=this.dup()
		ten:=fromLong(10)
		copy:=this.dup()
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
		int n=pos>>>5
		long k=(long)this.mag[this.cap-n-1]
		long m=1<<((long)pos&31)
		return k&m!=(long)0
	}
	setbit(int pos,bool val){
		int n=pos>>>5
		int m=1<<(pos&31)
		ind:=this.cap-n-1
		loc:=this.mag+ind<<2
		if val{
			*loc=*loc | m
		}else{
			*loc = *loc & (0-1 - m)
		}
	}
	toLong()long{
		long res=0
		for i:=this.size-1; i>=0; i--{
			res=res<<1
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
	print(1234)
		this.stripLeadingZero()
		nInts:=amount>>>5
		nBits:=amount&31
		magLen:=this.cap
		int* n
		nl:=magLen+nInts
		val:=this.mag
		if nBits==0{
			n=(int*)malloc((magLen+nInts)<<2)
			for i:=magLen; i<nl; i++{
				n[i]=0
			}
			for i:=0; i<magLen; i++{
				n[i]=val[i]
			}
			
		}else{
			i:=0
			nBits2:=32-nBits
			highBits:=val[0]>>>nBits2
			if highBits!=0{
				n=(int*)malloc((magLen+nInts+1)<<2)
				for k:=magLen-1; k<nl; k++{
					n[k]=0
				}
				nl++
				n[i]=highBits
				i++
			}else{
				if nInts==0{
					
			         int n2 = 32 - amount;
			         int k=0
			         c:=val[0]
			         m:=magLen-1
			         for k<m {
			             int b = c;
			             c = val[k+1];
			             val[k] = (b << amount) | (c >>> n2);
			             k++
			         }
			         val[m] = val[m]<<amount;
			         this.size=this.size+amount
					return
				}
				n=(int*)malloc((magLen+nInts)<<2)
				for k:=magLen; k<nl; k++{
					n[k]=0
				}
			}
			j:=0
			for j < magLen-1{
				n[i]=(val[j]<<nBits)|(val[j+1]>>>nBits2)
				j++
				i++
			}
			n[i]=val[j]<<nBits
		}
		free(val)
		this.mag=n
		this.size=nl<<5
		this.cap=nl
		this.stripLeadingZero()
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
	/*trim()bigint*{
		return this.padtolength(this.highestbitset()+1)
	}
	trimInPlace(){
		for i:=this.size-1;i>=0;i--{
			if this.bit(i){
				this.size=i+1
				return
			}
		}
		this.neg=false //cant have negative zero
		this.size=1
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
	}*/
	stripLeadingZero(){
		mc:=this.mag
		tc:=this.cap
		s:=false
		for i:=this.size-1;i>=0;i--{
			int n=i>>>5//inlined this.bit
			//why? because it made ec 30% faster
			long k=(long)mc[tc-n-1]
			long m=1<<((long)i&31)
			if k&m!=(long)0{
				this.size=i+1
				s=true
				break
			}
		}
		if !s{
			this.neg=false //cant have negative zero
			this.size=1
		}
		if tc<=1{
			return
		}
		count:=0
		for count < tc && mc[count]==0{
			count++
		}
		if(count==0){
			return
		}
		if count==this.cap{
			count--
		}
		n:=(int*)malloc((tc-count)<<2)
		for i:=tc-1; i>=count; i--{
			n[i-count]=mc[i]
		}
		free(mc)
		this.mag=n
		this.cap=tc-count
	}
	rightshiftone()bigint*{
		this.stripLeadingZero()
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
		copy=copy>>1
		count++
	}
	x:=new(count+1)
	for i:=0; inp>(long)0; i++{
		x.setbit(i,inp&(long)1==(long)1)
		inp=inp>>1
	}
	x.stripLeadingZero()
	x.neg=neg
	return x
}
new(int size)bigint*{
	res:=(bigint*)malloc(sizeof(bigint))
	res.size=size
	s:=size>>>5
	if size&31!=0{
		s++
	}
	res.cap=s
	res.mag=(int*)malloc(s<<2)
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
	a.stripLeadingZero()
	b.stripLeadingZero()

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
	res:=new(xIndex<<5)
	result:=res.mag
	long sum = 0
	mask:=mask(0)
	for yIndex > 0 {
		xIndex--
		yIndex--
		sum = ((long)x[xIndex] & mask) + ((long)y[yIndex] & mask) + (sum >>> 32);
		result[xIndex] = (int)sum;
	}
	bool carry = (sum >>> (long)32 != (long)0);
	for xIndex > 0 && carry {
		xIndex--
		tmp:=x[xIndex]+1
		result[xIndex]=tmp
		carry = tmp == 0
	}
	for xIndex > 0 {
		xIndex--
		result[xIndex] = x[xIndex];
	}
	if (carry) {
		newLen := res.cap + 1;
		temp:=(int*)malloc(newLen<<2)
		for i := 1; i<newLen; i++{
			temp[i] = result[i-1]
		}
		temp[0] = 1
		free(result)
		res.mag = temp
		res.cap=newLen
		res.size=res.size+1
	}
	res.stripLeadingZero()
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
	res:=new(bigIndex<<5)
	result := res.mag
	long difference = 0;
	mask:=mask(0)
	for(littleIndex > 0) {
		bigIndex--
		littleIndex--
		difference = ((long)big[bigIndex] & mask) -((long)little[littleIndex] & mask) +(difference >> 32);
		result[bigIndex] = (int)difference;
	}
	bool borrow = (difference >> (long)32 != (long)0);
	for (bigIndex > 0 && borrow){
		bigIndex--
		lol:=big[bigIndex] - 1
		result[bigIndex]=lol
		borrow = lol == (0-1);
	}
	for (bigIndex > 0){
		bigIndex--
		result[bigIndex] = big[bigIndex];
	}
	res.stripLeadingZero()
	return res
}
trustedSubtract(bigint* a,bigint* b)bigint*{
	a.stripLeadingZero()
	b.stripLeadingZero()
	big:=a.mag
	little:=b.mag
	int bigIndex = a.cap;
	int littleIndex = b.cap;
	res:=new(bigIndex<<5)
	result := res.mag
	long difference = 0;
	mask:=mask(0)
	for(littleIndex > 0) {
		bigIndex--
		littleIndex--
		difference = ((long)big[bigIndex] & mask) -((long)little[littleIndex] & mask) +(difference >> 32);
		result[bigIndex] = (int)difference;
	}
	bool borrow = (difference >> (long)32 != (long)0);
	for (bigIndex > 0 && borrow){
		bigIndex--
		lol:=big[bigIndex] - 1
		result[bigIndex]=lol
		borrow = lol == (0-1);
	}
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
	ah:=a.highestbitset()
	if ah!=b.highestbitset(){
		return false
	}
	for i:=ah; i>=0; i--{
		if a.bit(i)!=b.bit(i){
			return false
		}
	}
	return true
}

multiply(bigint* a, bigint* bb)bigint*{
bneg:=bb.neg
	aneg:=a.neg
	a.neg=false
	bb.neg=false


	a.stripLeadingZero()
	bb.stripLeadingZero()
	b:=bb
	mask:=mask(0)
	x:=a.mag
	y:=b.mag
	xlen:=a.cap
	ylen:=b.cap
	resultt:=new(32*(xlen+ylen))
	z:=resultt.mag
	int xstart = xlen - 1;
        int ystart = ylen - 1;

        long carry = 0;
        int j=ystart
        k:=ystart+1+xstart
        for (j>=0) {
            long product = ((long)y[j] & mask) *((long)x[xstart] & mask) + carry;
            z[k] = (int)product;
            carry = product >>> 32;
            j--
            k--
        }
        z[xstart] = (int)carry;

        for int i = xstart-1; i >= 0; i-- {
            carry = 0;
             j=ystart
            k=ystart+1+i
            for (j>=0) {
                long product = ((long)y[j] & mask) * ((long)x[i] & mask) +((long)z[k] & mask) + carry;
                z[k] = (int)product;
                carry = product >>> 32;
                j--
                k--
            }
            z[i] = (int)carry;
        }
        resultt.neg=aneg!=bneg
	a.neg=aneg
	bb.neg=bneg
	return resultt
	/*b=bb.padtolength(a.size+bb.size)
	bneg=bb.neg
	aneg=a.neg
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
	if !equal(res,resultt){
	print("no please")
		res.output()
		resultt.output()
		print("ok")
	}
	return res*/
}
dividemod(bigint*a,bigint*b)bigint*,bigint*{
	//print(50211)
	a.stripLeadingZero()
	bneg:=b.neg
	b.neg=false
	aneg:=a.neg
	a.neg=false
	d:=new(1)
	res:=new(a.size)
	dhs:=0-1
	bhs:=b.highestbitset()
	for e:=a.size-1;e>=0;e--{
		d.leftshift(1)
		dhs++
		d.setbit(0,a.bit(e))
		if dhs>=bhs{//keep track of the highest set bit to avoid full greater calc
			g:=greater(b,d)
			if(!g){
				old:=d
				d=trustedSubtract(d,b)
				dhs=d.highestbitset()
				old.free()
				res.setbit(e,true)
			}
		}
	}
	res.stripLeadingZero()
	d.stripLeadingZero()
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
	pow.stripLeadingZero()
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
	res.stripLeadingZero()
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
	res:=new(count<<2)
	for i:=0; i<count; i++{
		ch:=charFromHex(str[count-i-1])
		res.setbit(0+i<<2,ch&(byte)1!=(byte)0)
		res.setbit(1+i<<2,ch&(byte)2!=(byte)0)
		res.setbit(2+i<<2,ch&(byte)4!=(byte)0)
		res.setbit(3+i<<2,ch&(byte)8!=(byte)0)
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