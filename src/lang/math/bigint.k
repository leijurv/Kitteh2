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
			wew,rem:=dividemod(copy,ten)
			rem.free()
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
		//i:=0
		nl:=magLen+amount>>>5
		if amount&31==0{
			if amount>>>5==0{
				return
			}
			n22:=(int*)malloc((nl)<<2)
			for i2:=magLen; i2<nl; i2++{
				n22[i2]=0
			}
			for i3:=0; i3<magLen; i3++{
				n22[i3]=val[i3]
			}
			this.mag=n22
		}else{
			n:=(int*)malloc((nl+1)<<2)
			nBits:=amount&31
			nBits2:=32-nBits
			highBits:=val[0]>>>nBits2
			i:=(int)(highBits!=0)
			for k4:=magLen-i; k4<nl; k4++{
				n[k4]=0
			}
			nl=nl+i
			jj:=0
			n[jj]=highBits
			aaa:=magLen-1
			for jj < aaa{
				n[i]=(val[jj]<<nBits)|(val[jj+1]>>>nBits2)
				jj++
				i++
			}
			n[i]=val[jj]<<nBits
			this.mag=n
		}
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
	safeCopy()bigint*{
		this.stripLeadingZero()
		res:=new(this.size)
		for i:=0; i<this.size; i++{
			res.setbit(i,this.bit(i))
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
//adapted from jdk 6 BigInteger / MutableBigInteger
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
   	    return add(b,a)
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
dividemodOLD(bigint*a,bigint*b)bigint*,bigint*{
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






bitLen(int w) int{
        // Binary search - decision tree (5 tests, rarely 6)
        if (w < 0) {
            return 32;
        }
        for int i = 0; i < 32; i++ {
            if (w < (1 << i)) {
                return i;
            }
        }
        return 31;
    }
 unsignedLongCompare(long one, long two) bool{
 mv:=minval(0)
        return (one + mv) > (two + mv);
    }
minval(long inp)long{
	return (inp+1)<<63
}
/*main(){
print(minval(0)+(long)80)
	a:=new(96)
	a.mag[0]=5021
	a.mag[1]=5021
	a.mag[2]=5021
	a.size=96
	a.output()
	b:=new(32)
	b.mag[0]=420
	b.size=32
	b.output()
	res:=a.divide(b)

	for i:=0; i<res.cap; i++{
		print(res.mag[i])
	}
	print("rem")
	rem:=a.mod(b)
	for i:=0; i<rem.cap; i++{
		print(rem.mag[i])
	}

}*/
create(int lenInts)mutbig*{
	olen:=lenInts
	if(lenInts<=0){
		lenInts=1
	}
	a:=(mutbig*)malloc(sizeof(mutbig))
	av:=(int*)malloc(lenInts<<2)
	for i:=0; i<lenInts; i++{
		av[i]=0
	}
	a.value=av
	a.offset=0
	a.cap=lenInts
	a.intLen=olen
	return a
}
intmin(int i)int{
	return i<<31
}
BprimitiveRightShift(int* a, int len, int n) {
        int n2 = 32 - n;
        int i=len-1
        for int c = a[i]; i > 0; i-- {
            int b = c;
            c = a[i - 1];
            a[i] = (c << n2) | (b >>> n);
        }
        a[0] =a[0]>>> n;
    }
    // shifts a up to len left n bits assumes no leading zeros, 0<=n<32
 BprimitiveLeftShift(int* a, int len, int n) {
        if (len == 0 || n == 0) {
            return;
        }
        int n2 = 32 - n;
        int i=0
        int c=a[i]
        for int m = i + len - 1; i < m; i++ {
            int b = c;
            c = a[i + 1];
            a[i] = (b << n) | (c >>> n2);
        }
        a[len - 1] =a[len-1]<< n;
    }
 mulsub(int* q, int* a, int x, int len, int offset) int{
 LONGMASK:=mask(0)
        long xLong = (long)x & LONGMASK;
        long carry = 0;
        offset =offset+ len;
        for int j = len - 1; j >= 0; j--{
            long product = ((long)a[j] & LONGMASK) * xLong + carry;
            long difference = (long)q[offset] - product;
            q[offset] = (int) difference;
            offset--
            carry = (product >>> 32) + (long)((difference & LONGMASK)  > (( (long)((int)0-((int) product +(int)1)) & LONGMASK))) ;
        }
        return (int) carry;
    }
    divWord(int* result, long n, int d) {
        long dLong = (long)d & mask(0);
        if (dLong == (long)1) {
            result[0] = (int) n;
            result[1] = 0;
            return;
        }
        // Approximate the quotient and remainder
        long q = (n >>> 1) ÷ (dLong >>> 1);
        long r = n - q * dLong;
        // Correct the approximation
        for (r < (long)0) {
            r = dLong+r;
            q--;
        }
        for (r >= dLong) {
            r = r-dLong;
            q++;
        }
        // n - q*dlong == r && 0 <= r <dLong, hence we're done.
        result[0] = (int) q;
        result[1] = (int) r;
    }
    divadd(int* a, int alen, int* result, int offset) int{
        long carry = 0;
        LONGMASK:=mask(0)
        for int j = alen - 1; j >= 0; j-- {
            long sum = ((long)a[j] & LONGMASK) + ((long)result[j + offset] & LONGMASK) + carry;
            result[j + offset] = (int) sum;
            carry = sum >>> 32;
        }
        return (int) carry;
    }
struct mutbig{
	int* value
	int intLen
	int cap
	int offset
	free(){
		free(this.value)
		this.value=(int*)0
	}
	normalize() {
        if (this.intLen == 0) {
            this.offset = 0;
            return;
        }
        int index = this.offset;
        tv:=this.value
        if (tv[index] != 0) {
            return;
        }
        int indexBound = index + this.intLen;
        
            index++;
        for (index < indexBound && tv[index] == 0){
        	index ++
        }
        int numZeros = index - this.offset;
        this.intLen =this.intLen- numZeros;
        if this.intLen==0{
        	this.offset=0
        }else{
        this.offset=this.offset+numZeros
        }
    }
    primitiveLeftShift(int n) {
        int* val = this.value;
        int n2 = 32 - n;
        int i=this.offset
        int c=val[i]

        for int m = i + this.intLen - 1; i < m; i++ {
            int b = c;
            c = val[i + 1];
            val[i] = (b << n) | (c >>> n2);
        }
        loc:=(val+(this.offset + this.intLen - 1)<<2)
        *loc=((*loc)<< n);
    }
    primitiveRightShift(int n) {
        int* val = this.value;
        int n2 = 32 - n;
        to:=this.offset
        int i=to+this.intLen-1
        for int c = val[i]; i > to; i-- {
            int b = c;
            c = val[i - 1];
            val[i] = (c << n2) | (b >>> n);
        }
        val[to] =val[to]>>> n;
    }
     leftShift(int n) {
        /*
         * If there is enough storage space in this MutableBigInteger already
         * the available space will be used. Space to the right of the used
         * ints in the value array is faster to utilize, so the extra space
         * will be taken from the right if possible.
         */
        if (this.intLen == 0) {
            return;
        }
        int nInts = n >>> 5;
        int nBits = n & 31;
        int bitsInHighWord = bitLen(this.value[this.offset]);
        // If shift can be done without moving words, do so
        if (n <= (32 - bitsInHighWord)) {
            this.primitiveLeftShift(nBits);
            return;
        }
        int newLen = this.intLen + nInts + 1;
        if (nBits <= (32 - bitsInHighWord)) {
            newLen--;
        }
        if (this.cap < newLen||true) {
            // The array must grow
            vhar:=newLen
            if vhar<=0{
            	vhar=1
            }
            int* result = (int*)malloc(vhar<<2)
            for i:=0; i<vhar; i++{
            	result[i]=0
            }
            tv:=this.value
            for int i = 0; i < this.intLen; i++ {
                result[i] = tv[this.offset + i];
            }
            free(tv)
            this.value=result
            this.cap=vhar
            this.intLen=newLen
            this.offset=0
        } /*else {
	        if (this.cap >= newLen+this.offset) {
	            // Use space on right
	            for int i = 0; i < newLen - this.intLen; i++ {
	                this.value[this.offset + this.intLen + i] = 0;
	            }
	        } else {
	            // Must use space on left
	            for int i = 0; i < this.intLen; i++ {
	                this.value[i] = this.value[this.offset + i];
	            }
	            for int i = this.intLen; i < newLen; i++ {
	                this.value[i] = 0;
	            }
	            this.offset = 0;
	        }
        }*/
        this.intLen = newLen;
        if (nBits == 0) {
            return;
        }
        if (nBits <= (32 - bitsInHighWord)) {
            this.primitiveLeftShift(nBits);
        } else {
            this.primitiveRightShift(32 - nBits);
        }
    }

     rightShift(int n) {
        if (this.intLen == 0) {
            return;
        }
        int nInts = n >>> 5;
        int nBits = n & 31;
        this.intLen =this.intLen- nInts;
        if (nBits == 0) {
            return;
        }
        int bitsInHighWord = bitLen(this.value[this.offset]);
        if (nBits >= bitsInHighWord) {
            this.primitiveLeftShift(32 - nBits);
            this.intLen=this.intLen-1;
        } else {
            this.primitiveRightShift(nBits);
        }
    }
    divideOneWord(int divisor, mutbig* quotient) {
    LONGMASK:=mask(0)
        long divLong = (long)divisor & LONGMASK;
        // Special case of one word dividend
        if (this.intLen == 1) {
            long remValue = (long)this.value[this.offset] & LONGMASK;
            quotient.value[0] = (int) (remValue / divLong);
            quotient.intLen = (int)(quotient.value[0] != 0);
            quotient.offset = 0;
            this.value[0] = (int) (remValue - ((long)(quotient.value[0]) * divLong));
            this.offset = 0;
            this.intLen = (int)(this.value[0] != 0);
            return;
        }
        if (quotient.cap < this.intLen) {
        	free(quotient.value)
        	ql:=this.intLen
        	if ql<=0{
        		ql=1
        	}
        	qv:=(int*)malloc(ql<<2)
            quotient.value = qv
            for i:=0; i<ql; i++{
            	qv[i]=0
            }
            quotient.cap=ql
        }
        quotient.offset = 0;
        quotient.intLen = this.intLen;
        // Normalize the divisor
        int shift = 32 - bitLen(divisor);
        int rem = this.value[this.offset];
        long remLong = (long)rem & LONGMASK;
        if (remLong < divLong) {
            quotient.value[0] = 0;
        } else {
            quotient.value[0] = (int) (remLong / divLong);
            rem = (int) (remLong - ((long)(quotient.value[0]) * divLong));
            remLong = (long)rem & LONGMASK;
        }
        til:=this.intLen
        int xlen = til;
        int* qWord = (int*)malloc(8);
        qWord[0]=0
        qWord[1]=0
        xlen--
        tv:=this.value
        quev:=quotient.value
        for (xlen > 0) {
            long dividendEstimate = (remLong << 32)  | ((long)(tv[this.offset + til - xlen]) & LONGMASK);
            if (dividendEstimate >= (long)0) {
                qWord[0] = (int) (dividendEstimate / divLong);
                qWord[1] = (int) (dividendEstimate - ((long)qWord[0] * divLong));
            } else {
                divWord(qWord, dividendEstimate, divisor);
            }
            quev[til - xlen] = qWord[0];
            rem = qWord[1];
            remLong = (long)rem & LONGMASK;
            xlen--
        }
        // Unnormalize
        if (shift > 0) {
            rem=rem%divisor
       
        } 
            tv[0] = rem;
        
        this.intLen = (int)(tv[0] != 0) ;
        quotient.normalize();
        free(qWord)
    }
    clear() {
        this.offset = 0
        this.intLen = 0;
        int n=this.cap
        tv:=this.value
        for int index = 0; index < n; index++ {
            tv[index] = 0;
        }
    }
    copyValue(mutbig* val) {
        int len = val.intLen;
        tv:=this.value
        //if (this.cap< len) {
        	cop:=len
        	if cop<=0{
        		cop=1
        	}
        	free(tv)
            tv = (int*)malloc(cop<<2)
            this.value=tv
            this.cap=cop
        //}
        vv:=val.value
        vo:=val.offset
        for int i = 0; i < len; i++ {
            tv[i] = vv[vo + i];
        }
        this.intLen = len;
        this.offset = 0;
    }
     
      compare(mutbig* b) int{
        if (this.intLen < b.intLen) {
            return 0-1;
        }
        if (this.intLen > b.intLen) {
            return 1;
        }
        im:=intmin(1)
        for int i = 0; i < this.intLen; i++ {
            int b1 = this.value[this.offset + i] + im;
            int b2 = b.value[b.offset + i] + im;
            if (b1 < b2) {
                return 0-1;
            }
            if (b1 > b2) {
                return 1;
            }
        }
        return 0;
    }
    divide(mutbig* b, mutbig* quotient, mutbig* rem) {
    	if(this.intLen==0){
    	quotient.intLen=0
    	quotient.offset=0
    	rem.intLen=0
    	rem.offset=0
    	return
    	}
    	cmp:=this.compare(b)
    	if cmp<0{
    		quotient.intLen=0
    		quotient.offset=0
    		rem.copyValue(this)
    		return
    	}
    	if cmp==0{
    		quotient.value[0]=1
    		quotient.intLen=1
    		quotient.offset=0
    		rem.intLen=0
    		rem.offset=0
    		return
    	}
    	quotient.clear()
     	if(b.intLen==1){
     		rem.copyValue(this)
     		rem.divideOneWord(b.value[b.offset],quotient)
     		return
     	}

     	// Copy divisor value to protect divisor
     	int dlen = b.intLen;
     	whoaled:=dlen
     	if whoaled<=0{
     		whoaled=1
     	}
        int* d = (int*)malloc(whoaled<<2);
        d[0]=0
        bvv:=b.value
        boff:=b.offset
        for int i = 0; i < dlen; i++ {
            d[i] = bvv[boff+ i];
        }
        
        // Remainder starts as dividend with space for a leading zero
        if (rem.cap < this.intLen + 1) {
        	free(rem.value)
        	leee:=this.intLen+1
        	if leee<=0{
        		leee=1
        	}
        	rv:=(int*)malloc(leee<<2)
            rem.value =rv;
            for int i=0; i<leee; i++{
            	rv[i]=0
            }
            rem.cap=leee
        }
        rouv:=rem.value
        tev:=this.value
        tile:=this.intLen
        touf:=this.offset
        for int i = 0; i < tile; i++ {
            rouv[i + 1] = tev[i + touf];
        }
        rem.intLen = this.intLen;
        rem.offset = 1;
        int nlen = rem.intLen;
        // Set the quotient size
        int limit = nlen - dlen + 1;
        if (quotient.cap < limit) {
        	free(quotient.value)
        	vvv:=limit
        	if vvv<=0{
        		vvv=1
        	}
            qwav:= (int*)malloc(vvv<<2);
            quotient.value=qwav
            for i:=0; i<vvv; i++{
            	qwav[i]=0
            }
            quotient.cap=vvv
            
        }
        quotient.offset = 0;
        quotient.intLen = limit;
        int* q = quotient.value;
        // D1 normalize the divisor
        int shift = 32 - bitLen(d[0]);
        if (shift > 0) {
            // First shift will not grow array
            BprimitiveLeftShift(d, dlen, shift);
            // But this one might
            rem.leftShift(shift);
        }
        // Must insert leading 0 in rem if its length did not change
        if (rem.intLen == nlen) {
            rem.offset = 0;
            rem.value[0] = 0;
            rem.intLen=rem.intLen+1;
        }
         LONGMASK:=mask(0)
        int dh = d[0];
        long dhLong = (long)dh & LONGMASK;
        int dl = d[1];
        int* qWord = (int*)malloc(8)
        qWord[0]=0
        qWord[1]=0
        // D2 Initialize j
       intmin:=intmin(1)
       rhemv:=rem.value
        for int j = 0; j < limit; j++ {
            // D3 Calculate qhat
            // estimate qhat
            int qhat = 0;
            int qrem = 0;
            bool skipCorrection = false;
            int nh = rhemv[j + rem.offset];
            int nh2 = nh + intmin;
            int nm = rhemv[j + 1 + rem.offset];
            if (nh == dh) {
                qhat = 0-1;
                qrem = nh + nm;
                skipCorrection = qrem + intmin < nh2;
            } else {
                long nChunk = (((long) nh) << 32) | ((long)nm & LONGMASK);
                if (nChunk >= (long)0) {
                    qhat = (int) (nChunk / dhLong);
                    qrem = (int) (nChunk - ((long)qhat * dhLong));
                } else {
                    divWord(qWord, nChunk, dh);
                    qhat = qWord[0];
                    qrem = qWord[1];
                }
            }
            if (qhat == 0) {
                continue;
            }
            if (!skipCorrection) { // Correct qhat
                long nl = (long)rhemv[j + 2 + rem.offset] & LONGMASK;
                long rs = (((long)qrem & LONGMASK) << 32) | nl;
                long estProduct = ((long)dl & LONGMASK) * ((long)qhat & LONGMASK);
                if (unsignedLongCompare(estProduct, rs)) {
                    qhat--;
                    qrem = (int) (((long)qrem & LONGMASK) + dhLong);
                    if (((long)qrem & LONGMASK) >= dhLong) {
                        estProduct = ((long)dl & LONGMASK) * ((long)qhat & LONGMASK);
                        rs = (((long)qrem & LONGMASK) << 32) | nl;
                        if (unsignedLongCompare(estProduct, rs)) {
                            qhat--;
                        }
                    }
                }
            }
            // D4 Multiply and subtract
            rhemv[j + rem.offset] = 0;
            int borrow = mulsub(rhemv, d, qhat, dlen, j + rem.offset);
            // D5 Test remainder
            if (borrow + intmin> nh2) {
                // D6 Add back
                divadd(d, dlen,rhemv, j + 1 + rem.offset);
                qhat--;
            }
            // Store the quotient digit
            q[j] = qhat;
        } // D7 loop on j
        // D8 Unnormalize
        if (shift > 0) {
            rem.rightShift(shift);
        }
        rem.normalize();
        quotient.normalize();
        free(qWord)
        free(d)
     	return
     }
     toBigint()bigint*{
     	resLen:=this.intLen
     	if resLen==0{
     		resLen=1
     	}
     	res:=new(resLen * 32)
     	for i:=0; i<resLen; i++{
     		res.mag[i]=0
     	}
     	tv:=this.value
     	for i:=0; i<this.intLen; i++{
     		res.mag[i]=tv[i+this.offset]
     	}
     	res.size=resLen*32
     	return res
     }
}
dividemod(bigint* aIn8, bigint* bIn8) bigint*, bigint*{
/*aIn8=aIn8.safeCopy()
aIn8=aIn8.safeCopy()
bIn8=bIn8.safeCopy()
bIn8=bIn8.safeCopy()*/
	aIn8.stripLeadingZero()
	bIn8.stripLeadingZero()
	/*aIn:=aIn8.safeCopy().safeCopy().safeCopy()
	bIn:=bIn8.safeCopy().safeCopy().safeCopy()*/
	aIn:=aIn8.dup()
	bIn:=bIn8.dup()
	
	//queueueuue:=greater(bIn,aIn)
	bneg:=bIn.neg
	bIn.neg=false
	aneg:=aIn.neg
	aIn.neg=false
	//literallywhat:=greater(bIn,aIn)
	if(greater(bIn,aIn)){
		rss:=new(1)
		bIn.neg=bneg
		aIn.neg=aneg
		rss.neg=aneg!=bneg
		free(bIn)
		return rss, aIn
	}

	a:=create(aIn.cap)
	for i:=0; i<aIn.cap; i++{
		a.value[i]=aIn.mag[i]
	}
	b:=create(bIn.cap)
	for i:=0; i<bIn.cap; i++{
		b.value[i]=bIn.mag[i]
	}
	//dont:=greater(bIn,aIn)
	q:=create(1)
	q.clear()
	r:=create(1)
	r.clear()
	//cancer:=greater(bIn,aIn)
	//ahh:=aIn.mag[0]
	//bhh:=bIn.mag[0]
	a.divide(b,q,r)
	//aha:=aIn.mag[0]
	//bha:=bIn.mag[0]
	//howw:=greater(bIn,aIn)
	qOut:=q.toBigint()
	rOut:=r.toBigint()
	free(q)
	free(a)
	free(b)
	free(r)
	//qOut.stripLeadingZero()
	//rOut.stripLeadingZero()
	qOut.neg=aneg!=bneg
	rOut.neg=aneg
	//sketchy:=greater(bIn,aIn)
	//bIn.neg=bneg
	//aIn.neg=aneg
	free(aIn)
	free(bIn)
	/*qReal, rReal := dividemodOLD(aIn8,bIn8)
qReal=qReal.safeCopy()
rReal=rReal.safeCopy()

	
	if !equal(qReal,qOut) || !equal(rReal,rOut){
		print("Oh no")
		  print(aneg)
		print(bneg)
		print("wew")
		print(greater(bIn,aIn))
		print(sketchy)
		print("howw")
		print(howw)
		print("cancer")
		print(cancer)
		print(dont)
		print(literallywhat)
		print("highest")
		writeByte(10)
		print(ahh)
		print(bhh)
		print(aha)
		print(bha)
		
		print("ok then")
		writeByte(10)
		print((long)(a))
		print((long)(aIn.mag))
		print((long)(bIn.mag))

		print("ayy lmao")
		writeByte(10)
		print(queueueuue)
		aIn.output()
		print("Divided by")
		writeByte(10)
		bIn.output()
		print("aka")
		writeByte(10)
		aIn8.output()
		print("div by")
		writeByte(10)
		bIn8.output()
		qOut.output()
		rOut.output()
		print("Should have been")
		writeByte(10)
		qReal.output()
		rReal.output()
		print(equal(qReal,qOut))
		print(equal(rReal,rOut))
		print("man")
	}*/
	return qOut, rOut
}