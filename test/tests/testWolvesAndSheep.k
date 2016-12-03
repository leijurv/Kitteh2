func main(){
	wolves:=newBoard(5)
	attacked:=newBoard(5)
	seed:=(int*)malloc(sizeof(int))
	*seed=1
	for true{
		for x:=0; x<5; x++{
			for y:=0; y<5; y++{
				wolves[x][y]=0
				attacked[x][y]=0
			}
		}
		for i:=0; i<5; i++{
			x:=0
			y:=0
			for true{
				x=r(seed,5)
				y=r(seed,5)
				if wolves[x][y]==0{
					break
				}
			}
			wolves[x][y]=1
			for dirX:=0-1; dirX<=1; dirX++{
				for dirY:=0-1; dirY<=1; dirY++{
					if dirX==0 && dirY==0{
						continue
					}
					fillDirection(attacked,x,y,dirX,dirY)
				}
			}
		}
		open:=0
		for x:=0; x<5; x++{
			for y:=0; y<5; y++{
				if attacked[x][y]==0{
					open++
				}
			}
		}
		if open>=3{
			for x:=0; x<5; x++{
				for y:=0; y<5; y++{
					if wolves[x][y]==1{
						pr('W')
						continue
					}
					if attacked[x][y]==1{
						pr('+')
						continue
					}
					pr('S')
				}
				pr(10)
			}
			print(*seed)
			break
		}
	}
}
func fillDirection(int** board,int x,int y,int dirX,int dirY){
	for x>=0 && y>=0 && x<5 && y<5{
		board[x][y]=1
		x=x+dirX
		y=y+dirY
	}
}
func pr(byte b){
	tmp:=(byte*)malloc(2)
	tmp[0]=b
	tmp[1]=0
	print(tmp)
}
func r(int* seed,int max)int{
	*seed=(*seed*5023+257)%2147483647
	return *seed%max
}
func newBoard(int size) int**{
	board:=(int**)malloc(size*sizeof(int*))
	for i:=0; i<size; i++{
		board[i]=(int*)malloc(size*sizeof(int))
	}
	return board
}