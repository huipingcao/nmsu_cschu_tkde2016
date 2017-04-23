package sampling;

/**
 * The element used to keep the sample for one influenced (citing)
 * object's one position (s) 
 * 
 * @author administrator
 *
 */
public class SampleElementInfluenced implements Comparable<SampleElementInfluenced>, Cloneable {

    public int u;
    public int aspect;
	public int w; //w
	public int z; //aspect
    public int latent_aspect;
	public int b; //binary value, s
    public int uprime;

	public SampleElementInfluenced(int _u, int _a, int _w,  int _z, int _la, int _b, int _uprime)
	{
        u = _u;
        aspect = _a;
		w = _w;
		z = _z;
        latent_aspect = _la;
		b = _b;
        uprime = _uprime;
	}

    @Override
    public int compareTo(SampleElementInfluenced arg0) {
        if (u < arg0.u) return  (-1);
        else if (u > arg0.u) return 1;
        else {
            if (aspect < arg0.aspect) return (-1);
            else if (aspect > arg0.aspect) return 1;
            else {
                if(w < arg0.w) return (-1);
                else if (w > arg0.w) return 1;
                else{
                    if (z < arg0.z) return (-1);
                    else if (z > arg0.z) return 1;
                    else {
                        if (latent_aspect < arg0.latent_aspect) return -1;
                        else if (latent_aspect > arg0.latent_aspect) return 1;
                        else {
                            if( b < arg0.b) return (-1);
                            else if (b > arg0.b) return 1;
                            else return 0;
                        }
                    }
                }
            }
        }
    }
	
	public String toString()
	{
		String str = "[user"+u+"][aspect"+ aspect +"][word"+ w +"][z"+z+"]"+"[b"+b+"][up"+uprime+"]";
		return str;
	}

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
