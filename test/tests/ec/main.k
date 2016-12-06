import ../bigint/main b
import base
struct ecpoint{
	void* x
	void* y
}
main(){
	x:=(b::bigint*)base::basepointX()
	b::bigint* y=(b::bigint*)base::basepointY()
	modulus:=(b::bigint*)base::modulus()
	b::output(x)
	b::output(y)
	b::output(modulus)

	bp:=(ecpoint*)malloc(sizeof(ecpoint))
	bp.x=(void*)x
	bp.y=(void*)y

	/*t:=add(bp,bp,(void*)modulus)
	q:=add(t,bp,(void*)modulus)
	output(t)
	output(q)*/
	for i:=1; i<=20; i++{
		print(i)
		pow:=(void*)b::fromLong((long)i)
		pt:=multiply(bp,pow,(void*)modulus,bp)
		output(pt)
	}
	/*print(9000)
	pow:=(void*)b::fromLong((long)5)
	pt:=multiply(bp,pow,(void*)modulus,bp)
	output(pt)*/
}
output(ecpoint* t){
	x:=b::tostring((b::bigint*)t.x,',')
	print(x)
	b::output((b::bigint*)t.y)
}
multiply(ecpoint* base,void* POW,void* MOD,ecpoint* bp)ecpoint*{
	pow:=(b::bigint*)POW
	b::trimInPlace(pow)
	if pow.size==1 && pow.cont[0]{
		return base
	}
	if pow.size<2{
		return (ecpoint*)0
	}
	if pow.cont[0]{
		//odd
		
		one:=b::new(1)
		one.cont[0]=true
		powminus:=b::sub(pow,one)
		b::f(one)
		min:=multiply(base,(void*)powminus,MOD,bp)
		
		/*print(123123)
		output(base)
		b::output((b::bigint*)pow)
		output(min)*/
		res:=add(bp,min,MOD)
		//output(res)
		return res
	}
	
	shiftedPow:=b::rightshiftone(pow)
	wew:=multiply(base,(void*)shiftedPow,MOD,bp)
	wewwew:=add(wew,wew,MOD)
	/*print(321321)
	output(base)
	b::output((b::bigint*)pow)
	output(wewwew)*/
	return wewwew
}
add(ecpoint* p,ecpoint* q,void* MOD)ecpoint*{
	px:=(b::bigint*)p.x
	py:=(b::bigint*)p.y
	qx:=(b::bigint*)q.x
	qy:=(b::bigint*)q.y
	modulus:=(b::bigint*)MOD
	/* BigInteger s;
        if (q.x.equals(x)) {
            if (!q.y.equals(y)) {
                throw new IllegalStateException("kush");
            }
            BigInteger xs = x.multiply(x);
            s = xs.add(xs).add(xs).multiply(y.add(y).modInverse(modulus)).mod(modulus);
        } else {
            s = y.subtract(q.y).add(modulus).multiply(x.subtract(q.x).modInverse(modulus)).mod(modulus);
        }
        BigInteger xR = s.multiply(s).subtract(x).subtract(q.x).mod(modulus);
        BigInteger yR = modulus.subtract(y.add(s.multiply(xR.subtract(x))).mod(modulus));
        return new ECPoint(xR, yR);*/
    b::bigint* s
    if b::equal(qx,px){
    //print(10112)
    	xs:=b::multiply(px,px)
    	threexs:=b::add(xs,b::add(xs,xs))
    	invertedy:=b::modInverse(b::add(py,py),modulus)
    	s=b::mod(b::multiply(threexs,invertedy),modulus)
    }else{
    	//print(10111)
    	a:=b::add(modulus,b::sub(py,qy))
    	//b::output(a)
    	b:=b::modInverse(b::add(modulus,b::sub(px,qx)),modulus)
    	//b::output(b)
    	s=b::mod(b::multiply(a,b),modulus)
    }
    /*print(555021)
    print(555021)
    b::output(px)
    b::output(py)
    b::output(qx)
    b::output(qy)
    b::output(s)
    print(555022)
    print(555022)*/
    xr:=b::mod(b::sub(b::sub(b::multiply(s,s),px),qx),modulus)
    //b::output(xr)
    yr:=b::mod(b::add(modulus,b::mod(b::sub(modulus,b::add(py,b::multiply(s,b::sub(xr,px)))),modulus)),modulus)
    res:=(ecpoint*)malloc(sizeof(ecpoint))
    res.x=(void*)xr
    res.y=(void*)yr
    return res
}
