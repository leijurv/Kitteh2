import ../bigint/main
import base
struct ecpoint{
	void* x
	void* y
}
main(){
	x:=(bigint*)basepointX()
	bigint* y=(bigint*)basepointY()
	modulus:=(bigint*)modulus()
	output(x)
	output(y)
	output(modulus)

	bp:=(ecpoint*)malloc(sizeof(ecpoint))
	bp.x=(void*)x
	bp.y=(void*)y

	for i:=15; i<=20; i++{
		print(i)
		pow:=(void*)fromLong((long)i)
		pt:=multiplyec(dupec(bp),pow,(void*)modulus,bp)
		outputec(pt)
		clear(pt)
		f((bigint*)pow)
	}
	outputec(multiplyec(dupec(bp),(void*)fromLong(50215021),(void*)modulus,bp))
}
clear(ecpoint* a){
	f((bigint*)a.x)
	f((bigint*)a.y)
	a.x=(void*)0
	a.y=(void*)0
	free(a)
}
dupec(ecpoint* x)ecpoint*{
	new:=(ecpoint*)malloc(sizeof(ecpoint))
	new.x=(void*)dup((bigint*)x.x)
	new.y=(void*)dup((bigint*)x.y)
	return new
}
outputec(ecpoint* t){
	x:=tostring((bigint*)t.x,',')
	print(x)
	output((bigint*)t.y)
}
multiplyec(ecpoint* base,void* POW,void* MOD,ecpoint* bp)ecpoint*{
	pow:=(bigint*)POW
	trimInPlace(pow)
	if pow.size==1 && pow.cont[0]{
		return base
	}
	if pow.size<2{
		return (ecpoint*)0
	}
	if pow.cont[0]{
		//odd
		
		one:=new(1)
		one.cont[0]=true
		powminus:=sub(pow,one)
		f(one)

		min:=multiplyec(base,(void*)powminus,MOD,bp)

		f(powminus)

		res:=addec(bp,min,MOD)
		clear(min)

		return res
	}
	
	shiftedPow:=rightshiftone(pow)
	wew:=multiplyec(base,(void*)shiftedPow,MOD,bp)
	f(shiftedPow)
	wewwew:=addec(wew,wew,MOD)
	clear(wew)
	return wewwew
}
addec(ecpoint* p,ecpoint* q,void* MOD)ecpoint*{
	px:=(bigint*)p.x
	py:=(bigint*)p.y
	qx:=(bigint*)q.x
	qy:=(bigint*)q.y
	modulus:=(bigint*)MOD
    bigint* s
    if equal(qx,px){
    	xs:=multiply(px,px)
    	twoxs:=add(xs,xs)
    	threexs:=add(xs,twoxs)
    	f(xs)
    	f(twoxs)
    	twopy:=add(py,py)
    	invertedy:=modInverse(twopy,modulus)
    	f(twopy)
    	lmao:=multiply(threexs,invertedy)
    	f(threexs)
    	f(invertedy)
    	s=mod(lmao,modulus)
    	f(lmao)
    }else{
    	ydiff:=sub(py,qy)
    	a:=add(modulus,ydiff)
    	f(ydiff)
    	xdiff:=sub(px,qx)
    	posxdiff:=add(modulus,xdiff)
    	f(xdiff)
    	b:=modInverse(posxdiff,modulus)
    	f(posxdiff)
    	ab:=multiply(a,b)
    	f(a)
    	f(b)
    	s=mod(ab,modulus)
    	f(ab)
    }
    ss:=multiply(s,s)
    sspx:=sub(ss,px)
    f(ss)
    sspxqx:=sub(sspx,qx)
    f(sspx)
    xr:=mod(sspxqx,modulus)
    f(sspxqx)
    xdiff:=sub(xr,px)
    sxdiff:=multiply(s,xdiff)
    f(xdiff)
    pysx:=add(py,sxdiff)
    f(sxdiff)
    negpysx:=sub(modulus,pysx)
    f(pysx)
    wrap1:=mod(negpysx,modulus)
    f(negpysx)
    wrap2:=add(modulus,wrap1)
    f(wrap1)
    yr:=mod(wrap2,modulus)
    f(wrap2)
    f(s)
    res:=(ecpoint*)malloc(sizeof(ecpoint))
    res.x=(void*)xr
    res.y=(void*)yr
    return res
}
