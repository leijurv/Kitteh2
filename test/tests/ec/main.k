import ../bigint/main b
import base
struct ecpoint{
	void* x
	void* y
}
main(){
	x:=(b::bigint*)basepointX()
	b::bigint* y=(b::bigint*)basepointY()
	modulus:=(b::bigint*)modulus()
	b::output(x)
	b::output(y)
	b::output(modulus)

	bp:=(ecpoint*)malloc(sizeof(ecpoint))
	bp.x=(void*)x
	bp.y=(void*)y

	for i:=15; i<=20; i++{
		print(i)
		pow:=(void*)b::fromLong((long)i)
		pt:=multiply(dup(bp),pow,(void*)modulus,bp)
		output(pt)
		clear(pt)
		b::f((b::bigint*)pow)
	}
	output(multiply(dup(bp),(void*)b::fromLong(50215021),(void*)modulus,bp))
}
clear(ecpoint* a){
	b::f((b::bigint*)a.x)
	b::f((b::bigint*)a.y)
	a.x=(void*)0
	a.y=(void*)0
	free(a)
}
dup(ecpoint* x)ecpoint*{
	new:=(ecpoint*)malloc(sizeof(ecpoint))
	new.x=(void*)b::dup((b::bigint*)x.x)
	new.y=(void*)b::dup((b::bigint*)x.y)
	return new
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

		b::f(powminus)

		res:=add(bp,min,MOD)
		clear(min)

		return res
	}
	
	shiftedPow:=b::rightshiftone(pow)
	wew:=multiply(base,(void*)shiftedPow,MOD,bp)
	b::f(shiftedPow)
	wewwew:=add(wew,wew,MOD)
	clear(wew)
	return wewwew
}
add(ecpoint* p,ecpoint* q,void* MOD)ecpoint*{
	px:=(b::bigint*)p.x
	py:=(b::bigint*)p.y
	qx:=(b::bigint*)q.x
	qy:=(b::bigint*)q.y
	modulus:=(b::bigint*)MOD
    b::bigint* s
    if b::equal(qx,px){
    	xs:=b::multiply(px,px)
    	twoxs:=b::add(xs,xs)
    	threexs:=b::add(xs,twoxs)
    	b::f(xs)
    	b::f(twoxs)
    	twopy:=b::add(py,py)
    	invertedy:=b::modInverse(twopy,modulus)
    	b::f(twopy)
    	lmao:=b::multiply(threexs,invertedy)
    	b::f(threexs)
    	b::f(invertedy)
    	s=b::mod(lmao,modulus)
    	b::f(lmao)
    }else{
    	ydiff:=b::sub(py,qy)
    	a:=b::add(modulus,ydiff)
    	b::f(ydiff)
    	xdiff:=b::sub(px,qx)
    	posxdiff:=b::add(modulus,xdiff)
    	b::f(xdiff)
    	b:=b::modInverse(posxdiff,modulus)
    	b::f(posxdiff)
    	ab:=b::multiply(a,b)
    	b::f(a)
    	b::f(b)
    	s=b::mod(ab,modulus)
    	b::f(ab)
    }
    ss:=b::multiply(s,s)
    sspx:=b::sub(ss,px)
    b::f(ss)
    sspxqx:=b::sub(sspx,qx)
    b::f(sspx)
    xr:=b::mod(sspxqx,modulus)
    b::f(sspxqx)
    xdiff:=b::sub(xr,px)
    sxdiff:=b::multiply(s,xdiff)
    b::f(xdiff)
    pysx:=b::add(py,sxdiff)
    b::f(sxdiff)
    negpysx:=b::sub(modulus,pysx)
    b::f(pysx)
    wrap1:=b::mod(negpysx,modulus)
    b::f(negpysx)
    wrap2:=b::add(modulus,wrap1)
    b::f(wrap1)
    yr:=b::mod(wrap2,modulus)
    b::f(wrap2)
    b::f(s)
    res:=(ecpoint*)malloc(sizeof(ecpoint))
    res.x=(void*)xr
    res.y=(void*)yr
    return res
}
