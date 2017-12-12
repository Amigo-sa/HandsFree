//========================================================================
// This conversion was produced by the Free Edition of
// C++ to Java Converter courtesy of Tangible Software Solutions.
// Order the Premium Edition at https://www.tangiblesoftwaresolutions.com
//========================================================================

//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
///#if ! _SITENCODER_H
///#define _SITENCODER_H

public class gsit_state_s
{
	public int yl; // Locked or steady state step size multiplier.
	public int yu; // Unlocked or non-steady state step size multiplier.
	public int dms; // Short term energy estimate.
	public int dml; // Long term energy estimate.
	public int ap; // Linear weighting coefficient of 'yl' and 'yu'.

	public int[] a = new int[2]; // Coefficients of pole portion of prediction filter.
	public int[] b = new int[6]; // Coefficients of zero portion of prediction filter.
	public int[] pk = new int[2]; /* Signs of previous two samples of a partially
				 * reconstructed signal. */
	public short[] dq = new short[6]; /* int here fails in newupdate on encode!
				 * Previous 6 samples of the quantized difference
				 * signal represented in an internal floating point
				 * format.
				 */
	public int[] sr = new int[2]; /* Previous 2 samples of the quantized difference
				 * signal represented in an internal floating point
				 * format. */
	public int td; // delayed tone detect, new in 1988 version
}