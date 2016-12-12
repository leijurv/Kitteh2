import base
import ../bigint/bigint

main(){

	x:=basepointX()
	bigint* y=basepointY()
	modulus:=modulus()
	x.output()
	y.output()
	modulus.output()

	bp:=basepoint()
	for i:=15; i<=20; i++{
		print(i)
		pow:=fromLong((long)i)
		pt:=bp.dup().multiply(pow,modulus,bp)
		pt.output()
		pt.free()
		pow.free()
	}
	bp.dup().multiply(fromLong(50215021),modulus,bp).output()
}

struct ecpoint{
	bigint* x
	bigint* y
	output(){
		x:=(this.x).tostring(',')
		writeNullTerm(x)
		(this.y).output()
	}
	free(){
		(this.x).free()
		(this.y).free()
		this.x=(bigint*)0
		this.y=(bigint*)0
		free(this)
	}
	dup()ecpoint*{
		new:=(ecpoint*)malloc(sizeof(ecpoint))
		new.x=(this.x).dup()
		new.y=(this.y).dup()
		return new
	}
	add(ecpoint* q,bigint* modulus)ecpoint*{
		px:=this.x
		py:=this.y
		qx:=q.x
		qy:=q.y
	    bigint* s
	    if equal(qx,px){
	    	xs:=multiply(px,px)
	    	twoxs:=add(xs,xs)
	    	threexs:=add(xs,twoxs)
	    	(xs).free()
	    	(twoxs).free()
	    	twopy:=add(py,py)
	    	invertedy:=modInverse(twopy,modulus)
	    	(twopy).free()
	    	lmao:=multiply(threexs,invertedy)
	    	(threexs).free()
	    	(invertedy).free()
	    	s=mod(lmao,modulus)
	    	(lmao).free()
	    }else{
	    	ydiff:=sub(py,qy)
	    	a:=add(modulus,ydiff)
	    	(ydiff).free()
	    	xdiff:=sub(px,qx)
	    	posxdiff:=add(modulus,xdiff)
	    	(xdiff).free()
	    	b:=modInverse(posxdiff,modulus)
	    	(posxdiff).free()
	    	ab:=multiply(a,b)
	    	(a).free()
	    	(b).free()
	    	s=mod(ab,modulus)
	    	(ab).free()
	    }
	    ss:=multiply(s,s)
	    sspx:=sub(ss,px)
	    (ss).free()
	    sspxqx:=sub(sspx,qx)
	    (sspx).free()
	    xr:=mod(sspxqx,modulus)
	    (sspxqx).free()
	    xdiff:=sub(xr,px)
	    sxdiff:=multiply(s,xdiff)
	    (xdiff).free()
	    pysx:=add(py,sxdiff)
	    (sxdiff).free()
	    negpysx:=sub(modulus,pysx)
	    (pysx).free()
	    wrap1:=mod(negpysx,modulus)
	    (negpysx).free()
	    wrap2:=add(modulus,wrap1)
	    (wrap1).free()
	    yr:=mod(wrap2,modulus)
	    (wrap2).free()
	    (s).free()
	    res:=(ecpoint*)malloc(sizeof(ecpoint))
	    res.x=xr
	    res.y=yr
	    return res
	}
	multiply(bigint* pow,bigint* MOD,ecpoint* bp)ecpoint*{
		pow.trimInPlace()
		if pow.size==1 && pow.cont[0]{
			return this
		}
		if pow.size<2{
			return (ecpoint*)0
		}
		if pow.cont[0]{
			//odd
			
			one:=new(1)
			one.cont[0]=true
			powminus:=sub(pow,one)
			one.free()

			min:=this.multiply(powminus,MOD,bp)

			powminus.free()

			res:=bp.add(min,MOD)
			min.free()

			return res
		}
		
		shiftedPow:=pow.rightshiftone()
		wew:=this.multiply(shiftedPow,MOD,bp)
		shiftedPow.free()
		wewwew:=wew.add(wew,MOD)
		wew.free()
		return wewwew
	}
}