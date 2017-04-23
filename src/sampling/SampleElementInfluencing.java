package sampling;

/**
 * The element used to keep the sample for one influencing object's one position (s) 
 * influencing = cited
 * 
 * @author administrator
 *
 */
public class SampleElementInfluencing implements Comparable<SampleElementInfluencing>{
    public int u=0;
    public int aspect = 0;
	public int w =0; //w
	public int z=0; //z
	
	public SampleElementInfluencing(){;}
	
	public SampleElementInfluencing(int _u, int _a, int _w, int _z)
	{
        u = _u;
		aspect = _a;
		w = _w;
		z = _z;
	}
	
	@Override
	public int compareTo(SampleElementInfluencing arg0) {
        if (u < arg0.u) return -1;
        else if (u > arg0.u) return 1;
        else{
            if(aspect < arg0.aspect) return (-1);
            else if (aspect > arg0.aspect) return (1);
            else{
                if(w < arg0.w) return (-1);
                else if (w > arg0.w) return 1;
                else{
                    if (z < arg0.z) return (-1);
                    else if (z > arg0.z) return 1;
                    else return 0;
                }
            }
        }
	}
	
	public String toString()
	{
		String str = "[u"+u+"]"+"[aspect"+ aspect +"][word"+ w +"][z"+z+"]";
		return str;
	}
}
