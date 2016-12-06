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

	for i:=1; i<=20; i++{
		print(i)
		pow:=(void*)b::fromLong((long)i)
		pt:=multiply(dup(bp),pow,(void*)modulus,bp)
		output(pt)
	}
}
clear(ecpoint* a){
	b::f((b::bigint*)a.x)
	b::f((b::bigint*)a.y)
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
    	twopy:=b::add(py,py)
    	invertedy:=b::modInverse(twopy,modulus)
    	lmao:=b::multiply(threexs,invertedy)
    	s=b::mod(lmao,modulus)
    }else{
    	a:=b::add(modulus,b::sub(py,qy))
    	b:=b::modInverse(b::add(modulus,b::sub(px,qx)),modulus)
    	s=b::mod(b::multiply(a,b),modulus)
    }
    xr:=b::mod(b::sub(b::sub(b::multiply(s,s),px),qx),modulus)
    yr:=b::mod(b::add(modulus,b::mod(b::sub(modulus,b::add(py,b::multiply(s,b::sub(xr,px)))),modulus)),modulus)
    res:=(ecpoint*)malloc(sizeof(ecpoint))
    res.x=(void*)xr
    res.y=(void*)yr
    return res
}
