struct bigint{
	int size
	int* mag
	int cap
	bool neg

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
		loc:=this.mag+(this.cap-(pos>>>5)-1)<<2
		/*int m=1<<(pos&31)
		if val{
			*loc=*loc | m
		}else{
			*loc = *loc & (0-1 - m)
		}*/
		*loc=(*loc&(0-1 - (1<<(pos&31))))|(((int)val)<<(pos&31)) //excessive parentheses because i don't remember order of operations
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
	leftshiftone(){
		vval:=this.mag
		c:=vval[0]
		if c>>>31==0{
			tc:=this.cap
			s:=false
			for iiii:=this.size-1;iiii>=0;iiii--{
				if ((long)vval[tc-iiii>>>5-1])&((long)1<<((long)iiii&(long)31))!=(long)0{
					this.size=iiii+2
					s=true
					break
				}
			}
			if !s{
				this.neg=false //cant have negative zero
				this.size=1
				return
			}
	        int k2=0
	        
	        m:=tc-1
	        for k2<m {
	            int b = c;
	            c = vval[k2+1];
	            vval[k2] = (b << 1) | (c >>> 31);
	            k2++
	        }
	        vval[m] = vval[m]<<1;
			return
		}
		this.leftshift(1)
	}
	leftshift(int amount){
		vval:=this.mag
		c:=vval[0]
		if (amount&31)!=0 && (amount>>>5)==0 && c>>>(32-(amount&31))==0{
			tc:=this.cap
			s:=false
			for iiii:=this.size-1;iiii>=0;iiii--{
				if ((long)vval[tc-iiii>>>5-1])&((long)1<<((long)iiii&(long)31))!=(long)0{
					this.size=iiii+1+amount
					s=true
					break
				}
			}
			if !s{
				this.neg=false //cant have negative zero
				this.size=1
				return
			}
			int n2 = 32 - amount;
	        int k2=0
	        
	        m:=tc-1
	        bbb:=32-n2
	        for k2<m {
	            int b = c;
	            c = vval[k2+1];
	            vval[k2] = (b << bbb) | (c >>> n2);
	            k2++
	        }
	        vval[m] = vval[m]<<bbb;
			return
		}
		this.stripLeadingZero()
		val:=this.mag
		magLen:=this.cap
		
		n:=(int*)0
		i:=0
		nl:=magLen+amount>>>5
		if amount&31==0{
			if amount>>>5==0{
				return
			}
			n=(int*)malloc((nl)<<2)
			for i2:=magLen; i2<nl; i2++{
				n[i2]=0
			}
			for i3:=0; i3<magLen; i3++{
				n[i3]=val[i3]
			}
		}else{

			highBits:=val[0]>>>(32-(amount&31))
			if highBits!=0{
				n=(int*)malloc((nl+1)<<2)
				for k4:=magLen-1; k4<nl; k4++{
					n[k4]=0
				}
				nl++
				n[i]=highBits
				i++
			}else{
			
				n=(int*)malloc((nl)<<2)
				for k3:=nl-1; k3>=magLen; k3--{
					n[k3]=0
				}
			}
			jj:=0
			nBits:=amount&31
			nBits2:=32-nBits
			aaa:=magLen-1
			for jj < aaa{
				n[i]=(val[jj]<<nBits)|(val[jj+1]>>>nBits2)
				jj++
				i++
			}
			n[i]=val[jj]<<nBits
		}
		this.mag=n
		this.size=nl<<5
		this.cap=nl
		free(val)
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
		n2:=(int*)malloc((tc-count)<<2)
		for i2:=tc-1; i2>=count; i2--{
			n2[i2-count]=mc[i2]
		}
		
		this.mag=n2
		free(mc)
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
	cc:=(int*)malloc(s<<2)
	for i:=0; i<s; i++{
		cc[i]=0
	}
	res.mag=cc
	res.neg=false
	return res
}
add(bigint* a,bigint* b)bigint*{
	if a.neg{
		if b.neg{
			a.neg=false
			b.neg=false
			res1:=add(a,b)
			res1.neg=true
			a.neg=true
			b.neg=true
			return res1
		}
		//b-a
		a.neg=false
		res2:=b.subtract(a)
		a.neg=true
		return res2
	}
	if b.neg{
		//a-b
		b.neg=false
		res3:=a.subtract(b)
		b.neg=true
		return res3
	}
	a.stripLeadingZero()
	b.stripLeadingZero()

 	// If x is shorter, swap the two arrays
   	if (a.cap < b.cap) {
   	//print("SWITCHEROO")
       bigint* copy1= a;
       a = b;
       b = copy1;
   	}
   	res:=new(a.cap<<5)
	x:=a.mag
	y:=b.mag
	int xIndex = a.cap;
	int yIndex = b.cap;
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
			res1:=b.subtract(a)
			b.neg=true
			a.neg=true
			return res1
		}
		//-a - b
		//-(a+b)
		a.neg=false
		res2:=add(a,b)
		a.neg=true
		res2.neg=true
		return res2
	}
	if b.neg{
		//a - -b
		//a+b
		b.neg=false
		res3:=add(a,b)
		b.neg=true
		return res3
	}
	a.stripLeadingZero()
	b.stripLeadingZero()
	g:=greater(b,a)
	if g {
		res4:=b.subtract(a)
		res4.neg=true
		return res4
	}
	res:=new(a.cap<<5)
	big:=a.mag
	little:=b.mag
	int bigIndex = a.cap;
	int littleIndex = b.cap;
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
	res:=new(a.cap<<5)
	big:=a.mag
	little:=b.mag
	int bigIndex = a.cap;
	int littleIndex = b.cap;
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

multiply(bigint* a, bigint* b)bigint*{
	a.stripLeadingZero()
	b.stripLeadingZero()
	resultt:=new((a.cap+b.cap)<<5)
	mask:=mask(0)
	x:=a.mag
	y:=b.mag
	
	z:=resultt.mag
	int xstart = a.cap - 1;
    ystart:=b.cap-1

    long carry = 0;
    int j=b.cap-1
    k:=b.cap+xstart
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
         jj:=ystart
        kk:=jj+1+i
        for (jj>=0) {
            long product2 = ((long)y[jj] & mask) * ((long)x[i] & mask) +((long)z[kk] & mask) + carry;
            z[kk] = (int)product2;
            carry = product2 >>> 32;
            jj--
            kk--
        }
        z[i] = (int)carry;
    }
    resultt.neg=a.neg!=b.neg
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
		b.leftshiftone()
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
	a.stripLeadingZero()
	b.stripLeadingZero()
	bneg:=b.neg
	b.neg=false
	aneg:=a.neg
	a.neg=false
	if(greater(b,a)){
		rss:=new(1)
		b.neg=bneg
		a.neg=aneg
		rss.neg=aneg!=bneg
		return rss, a.dup()
	}
	d:=new(1)
	res:=new(a.size)
	dhs:=0-1
	bhs:=b.highestbitset()
	for e:=a.size-1;e>=0;e--{
		d.leftshiftone()
		dhs++
		loc:=d.mag+(d.cap-1)<<2
		*loc=(*loc&(0-2))|(((int)(a.bit(e))))
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
	//t:=fromLong(1)
	//oldt:=fromLong(0)
	r:=b.dup()
	oldr:=a.dup()
	zero:=fromLong(0)
	for greater(r,zero){
		quotient:=divide(oldr,r)
		qr:=multiply(quotient,r)
		qs:=multiply(quotient,s)
		//qt:=multiply(quotient,t)
		newr:=oldr.subtract(qr)
		news:=olds.subtract(qs)
		//newt:=oldt.subtract(qt)
		(quotient).free()
		(qr).free()
		(qs).free()
		//(qt).free()
		(oldr).free()
		(olds).free()
		//(oldt).free()
		oldr=r
		olds=s
		//oldt=t
		r=newr
		s=news
		//t=newt
	}
	(zero).free()
	(r).free()
	(s).free()
	//(t).free()
	(oldr).free()
	//(oldt).free()
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
		return (0-'0')+char
	}
	if char>='a' && char<='f'{
		return (10-'a')+char
	}
	if char>='A' && char<='F'{
		return (10-'A')+char
	}
	return char
}
mask(int i) long{
	return (2<<((long)i+31))-1
}