//========================================================================
// This conversion was produced by the Free Edition of
// C++ to Java Converter courtesy of Tangible Software Solutions.
// Order the Premium Edition at https://www.tangiblesoftwaresolutions.com
//========================================================================

public class GlobalMembers
{
	//C++ TO JAVA CONVERTER TODO TASK: Typedefs defined in multiple preprocessor conditionals can only be replaced within the scope of the preprocessor conditional:
	//typedef gsit_state_s gsit_state;

	public static void gsit_init_state(gsit_state_s state_ptr)
	{
		int cnta;

		state_ptr.yl = 34816;
		state_ptr.yu = 544;
		state_ptr.dms = 0;
		state_ptr.dml = 0;
		state_ptr.ap = 0;
		for (cnta = 0; cnta < 2; cnta++)
		{
			state_ptr.a[cnta] = 0;
			state_ptr.pk[cnta] = 0;
			state_ptr.sr[cnta] = 32;
		}
		for (cnta = 0; cnta < 6; cnta++)
		{
			state_ptr.b[cnta] = 0;
			state_ptr.dq[cnta] = 32;
		}
		state_ptr.td = 0;
	}

	//C++ TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
	//ORIGINAL LINE: extern void SitEncoder(short *u, unsigned char *y, gsit_state_s *state_ptr);
	public static void SitEncoder(short[] u, byte[] y, gsit_state_s state_ptr)
	{

		int i;
	//C++ TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
	//ORIGINAL LINE: unsigned char smp;
		byte smp;


		for (i = 0; i < 80; i += 8)
		{
	//C++ TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
	//ORIGINAL LINE: smp = (unsigned char)gsit_16_encoder((int)u[i], state_ptr);
			smp = (byte)gsit_16_encoder((int)u[i], state_ptr);
	//C++ TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
	//ORIGINAL LINE: smp |=(((unsigned char)gsit_16_encoder((int)u[i+2], state_ptr)) << 2);
			smp |= (((byte)gsit_16_encoder((int)u[i + 2], state_ptr)) << 2);
	//C++ TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
	//ORIGINAL LINE: smp |=(((unsigned char)gsit_16_encoder((int)u[i+4], state_ptr)) << 4);
			smp |= (((byte)gsit_16_encoder((int)u[i + 4], state_ptr)) << 4);
	//C++ TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
	//ORIGINAL LINE: smp |=(((unsigned char)gsit_16_encoder((int)u[i+6], state_ptr)) << 6);
			smp |= (((byte)gsit_16_encoder((int)u[i + 6], state_ptr)) << 6);
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			y[i >> 3] = smp;
		}

	}
	//C++ TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
	//ORIGINAL LINE: extern void SitDecoder(unsigned char *u, short *y, gsit_state_s *state_ptr);
	public static void SitDecoder(byte[] u, short[] y, gsit_state_s state_ptr)
	{

		int i;
		int smp;




		for (i = 0; i < 10; i++)
		{
			smp = (int)(u[i] & 0x03);
			y[(i << 3)] = (short)gsit_16_decoder(smp, state_ptr);

//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			smp = (int)((u[i] >> 2) & 0x03);
			y[(i << 3) + 2] = (short)gsit_16_decoder(smp, state_ptr);

//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			smp = (int)((u[i] >> 4) & 0x03);
			y[(i << 3) + 4] = (short)gsit_16_decoder(smp, state_ptr);

//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			smp = (int)((u[i] >> 6) & 0x03);
			y[(i << 3) + 6] = (short)gsit_16_decoder(smp, state_ptr);

		}

		for (i = 1; i < 79; i += 2)
		{
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			y[i] = (short)(((int)((int)y[i - 1] + (int)y[i + 1])) >> 1);
		}
		y[79] = y[78];

	}


///#endif

	//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	///#if ! abs
	//C++ TO JAVA CONVERTER TODO TASK: #define macros defined in multiple preprocessor conditionals can only be replaced within the scope of the preprocessor conditional:
	///#define abs(n) ((n)<0 ? -(n) : (n))
	///#endif

	public static int[] power2 = {1, 2, 4, 8, 0x10, 0x20, 0x40, 0x80, 0x100, 0x200, 0x400, 0x800, 0x1000, 0x2000, 0x4000};

//C++ TO JAVA CONVERTER TODO TASK: Pointer arithmetic is detected on the parameter 'table', so pointers on this parameter are left unchanged:
	public static int quan(int val, int * table, int size)
	{
		int i;

		for (i = 0; i < size; i++)
		{
			if (val < *table++)
			{
				break;
			}
		}
		return (i);
	}

	public static int fmult(int an, int srn)
	{
		int anmag;
		int anexp;
		int anmant;
		int wanexp;
		int wanmant;
		int retval;

		anmag = (an > 0) ? an : ((-an) & 0x1FFF);
		anexp = quan(anmag, power2, 15) - 6;
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		anmant = (anmag == 0) ? 32 : (anexp >= 0) ? anmag >> anexp : anmag << -anexp;
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		wanexp = anexp + ((srn >> 6) & 0xF) - 13;

//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		wanmant = (anmant * (srn & 077) + 0x30) >> 4;
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		retval = (wanexp >= 0) ? ((wanmant << wanexp) & 0x7FFF) : (wanmant >> -wanexp);

		return (((an ^ srn) < 0) ? -retval : retval);
	}

	public static int predictor_zero(gsit_state_s state_ptr)
	{
		int i;
		int sezi;

//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		sezi = fmult(state_ptr.b[0] >> 2, state_ptr.dq[0]);
		for (i = 1; i < 6; i++) // ACCUM
		{
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			sezi += fmult(state_ptr.b[i] >> 2, state_ptr.dq[i]);
		}
		return (sezi);
	}

	public static int predictor_pole(gsit_state_s state_ptr)
	{
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		return (fmult(state_ptr.a[1] >> 2, state_ptr.sr[1]) + fmult(state_ptr.a[0] >> 2, state_ptr.sr[0]));
	}

	public static int step_size(gsit_state_s state_ptr)
	{
		int y;
		int dif;
		int al;

		if (state_ptr.ap >= 256)
		{
			return (state_ptr.yu);
		}
		else
		{
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			y = state_ptr.yl >> 6;
			dif = state_ptr.yu - y;
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			al = state_ptr.ap >> 2;
			if (dif > 0)
			{
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
				y += (dif * al) >> 6;
			}
			else if (dif < 0)
			{
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
				y += (dif * al + 0x3F) >> 6;
			}
			return (y);
		}
	}

	public static int quantize(int d, int y, tangible.RefObject<Integer> table, int size) // table size of integers -  quantization table -  Step size multiplier -  Raw difference signal sample
	{
		int dqm; // Magnitude of 'd'
		int exp; // Integer part of base 2 log of 'd'
		int mant; // Fractional part of base 2 log
		int dl; // Log of magnitude of 'd'
		int dln; // Step size scale factor normalized log
		int i;

		/*
		 * LOG
		 *
		 * Compute base 2 log of 'd', and store in 'dl'.
		 */
		dqm = ((d) < 0 ? -(d) : (d));
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		exp = quan(dqm >> 1, power2, 15);
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		mant = ((dqm << 7) >> exp) & 0x7F; // Fractional portion.
		dl = (exp << 7) + mant;

		/*
		 * SUBTB
		 *
		 * "Divide" by step size multiplier.
		 */
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		dln = dl - (y >> 2);

		/*
		 * QUAN
		 *
		 * Obtain codword i for 'd'.
		 */
		i = quan(dln, table.argValue, size);
		if (d < 0) // take 1's complement of i
		{
			return ((size << 1) + 1 - i);
		}
		else if (i == 0) // take 1's complement of 0
		{
			return ((size << 1) + 1); // new in 1988
		}
		else
		{
			return (i);
		}
	}

	public static int reconstruct(int sign, int dqln, int y) // Step size multiplier -  G.72x codeword -  0 for non-negative value
	{
		int dql; // Log of 'dq' magnitude
		int dex; // Integer part of log
		int dqt;
		int dq; // Reconstructed difference signal sample

//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		dql = dqln + (y >> 2); // ADDA

		if (dql < 0)
		{
			return ((sign) != 0 ? -0x8000 : 0);
		}
		else
		{ // ANTILOG
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			dex = (dql >> 7) & 15;
			dqt = 128 + (dql & 127);
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			dq = (short)((dqt << 7) >> (14 - dex));
			return ((sign) != 0 ? (dq - 0x8000) : dq);
		}
	}

	public static short[] _dqlntab = {116, 365, 365, 116};
	public static short[] _witab = {-704, 14048, 14048, -704};
	public static short[] _fitab = {0, 0xE00, 0xE00, 0};
	public static int[] qtab_723_16 = {261};

	public static void update(int code_size, int y, int wi, int fi, int dq, int sr, int dqsez, gsit_state_s state_ptr) // coder state pointer -  difference from 2-pole predictor -  reconstructed signal -  quantized prediction difference -  for long/short term energies -  scale factor multiplier -  quantizer step size -  distinguish 723_40 with others
	{
		int cnt;
		int mag; // Adaptive predictor, FLOAT A
		int exp;
		int a2p; // LIMC
		int a1ul; // UPA1
		int pks1; // UPA2
		int fa1;
		int tr; // tone/transition detector
		int ylint;
		int thr2;
		int dqthr;
		int ylfrac;
		int thr1;
		int pk0;

		pk0 = (dqsez < 0) ? 1 : 0; // needed in updating predictor poles

		mag = dq & 0x7FFF; // prediction difference magnitude
		/* TRANS */
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		ylint = state_ptr.yl >> 15; // exponent part of yl
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		ylfrac = (state_ptr.yl >> 10) & 0x1F; // fractional part of yl
		thr1 = (32 + ylfrac) << ylint; // threshold
		thr2 = (ylint > 9) ? 31 << 10 : thr1; // limit thr2 to 31 << 10
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		dqthr = (thr2 + (thr2 >> 1)) >> 1; // dqthr = 0.75 * thr2
		if (state_ptr.td == 0) // signal supposed voice
		{
			tr = 0;
		}
		else if (mag <= dqthr) // supposed data, but small mag
		{
			tr = 0; // treated as voice
		}
		else // signal is data (modem)
		{
			tr = 1;
		}

		/*
		 * Quantizer scale factor adaptation.
		 */

		/* FUNCTW & FILTD & DELAY */
		/* update non-steady state step size multiplier */
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		state_ptr.yu = y + ((wi - y) >> 5);

		/* LIMB */
		if (state_ptr.yu < 544) // 544 <= yu <= 5120
		{
			state_ptr.yu = 544;
		}
		else if (state_ptr.yu > 5120)
		{
			state_ptr.yu = 5120;
		}

		/* FILTE & DELAY */
		/* update steady state step size multiplier */
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		state_ptr.yl += state_ptr.yu + ((-state_ptr.yl) >> 6);

		/*
		 * Adaptive predictor coefficients.
		 */
		if (tr == 1)
		{ // reset a's and b's for modem signal
			state_ptr.a[0] = 0;
			state_ptr.a[1] = 0;
			state_ptr.b[0] = 0;
			state_ptr.b[1] = 0;
			state_ptr.b[2] = 0;
			state_ptr.b[3] = 0;
			state_ptr.b[4] = 0;
			state_ptr.b[5] = 0;
			a2p = 0;
		}
		else
		{ // update a's and b's
			pks1 = pk0 ^ state_ptr.pk[0]; // UPA2

			/* update predictor pole a[1] */
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			a2p = state_ptr.a[1] - (state_ptr.a[1] >> 7);
			if (dqsez != 0)
			{
				fa1 = (pks1) != 0 ? state_ptr.a[0] : -state_ptr.a[0];
				if (fa1 < -8191) // a2p = function of fa1
				{
					a2p -= 0x100;
				}
				else if (fa1 > 8191)
				{
					a2p += 0xFF;
				}
				else
				{
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
					a2p += fa1 >> 5;
				}

				if ((pk0 ^ state_ptr.pk[1]) != 0)
				{
					/* LIMC */
					if (a2p <= -12160)
					{
						a2p = -12288;
					}
					else if (a2p >= 12416)
					{
						a2p = 12288;
					}
					else
					{
						a2p -= 0x80;
					}
				}
				else if (a2p <= -12416)
				{
					a2p = -12288;
				}
				else if (a2p >= 12160)
				{
					a2p = 12288;
				}
				else
				{
					a2p += 0x80;
				}
			}

			/* TRIGB & DELAY */
			state_ptr.a[1] = a2p;

			/* UPA1 */
			/* update predictor pole a[0] */
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			state_ptr.a[0] -= state_ptr.a[0] >> 8;
			if (dqsez != 0)
			{
				if (pks1 == 0)
				{
					state_ptr.a[0] += 192;
				}
				else
				{
					state_ptr.a[0] -= 192;
				}
			}

			/* LIMD */
			a1ul = 15360 - a2p;
			if (state_ptr.a[0] < -a1ul)
			{
				state_ptr.a[0] = -a1ul;
			}
			else if (state_ptr.a[0] > a1ul)
			{
				state_ptr.a[0] = a1ul;
			}

			/* UPB : update predictor zeros b[6] */
			for (cnt = 0; cnt < 6; cnt++)
			{
				if (code_size == 5) // for 40Kbps G.723
				{
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
					state_ptr.b[cnt] -= state_ptr.b[cnt] >> 9;
				}
				else // for G.721 and 24Kbps G.723
				{
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
					state_ptr.b[cnt] -= state_ptr.b[cnt] >> 8;
				}
				if ((dq & 0x7FFF) != 0)
				{ // XOR
					if ((dq ^ state_ptr.dq[cnt]) >= 0)
					{
						state_ptr.b[cnt] += 128;
					}
					else
					{
						state_ptr.b[cnt] -= 128;
					}
				}
			}
		}

		for (cnt = 5; cnt > 0; cnt--)
		{
			state_ptr.dq[cnt] = state_ptr.dq[cnt - 1];
		}
		/* FLOAT A : convert dq[0] to 4-bit exp, 6-bit mantissa f.p. */
		if (mag == 0)
		{
			state_ptr.dq[0] = (dq >= 0) ? 0x20 : 0xFC20;
		}
		else
		{
			exp = quan(mag, power2, 15);
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			state_ptr.dq[0] = (short)((dq >= 0) ? (exp << 6) + ((mag << 6) >> exp) : (exp << 6) + ((mag << 6) >> exp) - 0x400);
		}

		state_ptr.sr[1] = state_ptr.sr[0];
		/* FLOAT B : convert sr to 4-bit exp., 6-bit mantissa f.p. */
		if (sr == 0)
		{
			state_ptr.sr[0] = 0x20;
		}
		else if (sr > 0)
		{
			exp = quan(sr, power2, 15);
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			state_ptr.sr[0] = (exp << 6) + ((sr << 6) >> exp);
		}
		else if (sr > -32768)
		{
			mag = -sr;
			exp = quan(mag, power2, 15);
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			state_ptr.sr[0] = (exp << 6) + ((mag << 6) >> exp) - 0x400;
		}
		else
		{
			state_ptr.sr[0] = 0xFC20;
		}

		/* DELAY A */
		state_ptr.pk[1] = state_ptr.pk[0];
		state_ptr.pk[0] = pk0;

		/* TONE */
		if (tr == 1) // this sample has been treated as data
		{
			state_ptr.td = 0; // next one will be treated as voice
		}
		else if (a2p < -11776) // small sample-to-sample correlation
		{
			state_ptr.td = 1; // signal may be data
		}
		else // signal is voice
		{
			state_ptr.td = 0;
		}

		/*
		 * Adaptation speed control.
		 */
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		state_ptr.dms += (fi - state_ptr.dms) >> 5; // FILTA
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		state_ptr.dml += (((fi << 2) - state_ptr.dml) >> 7); // FILTB

		if (tr == 1)
		{
			state_ptr.ap = 256;
		}
		else if (y < 1536) // SUBTC
		{
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			state_ptr.ap += (0x200 - state_ptr.ap) >> 4;
		}
		else if (state_ptr.td == 1)
		{
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			state_ptr.ap += (0x200 - state_ptr.ap) >> 4;
		}
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		else if ((((state_ptr.dms << 2) - state_ptr.dml) < 0 ? -((state_ptr.dms << 2) - state_ptr.dml) : ((state_ptr.dms << 2) - state_ptr.dml)) >= (state_ptr.dml >> 3))
		{
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			state_ptr.ap += (0x200 - state_ptr.ap) >> 4;
		}
		else
		{
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
			state_ptr.ap += (-state_ptr.ap) >> 4;
		}
	}

	public static int gsit_16_decoder(int i, gsit_state_s state_ptr)
	{
		int sezi;
		int sez; // ACCUM
		int sei;
		int se;
		int y; // MIX
		int dq;
		int sr; // ADDB
		int dqsez;

		i &= 0x03; // mask to get proper bits
		sezi = predictor_zero(state_ptr);
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		sez = sezi >> 1;
		sei = sezi + predictor_pole(state_ptr);
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		se = sei >> 1; // se = estimated signal

		y = step_size(state_ptr); // adaptive quantizer step size
		dq = reconstruct(i & 0x02, _dqlntab[i], y); // unquantize pred diff

		sr = (dq < 0) ? (se - (dq & 0x3FFF)) : (se + dq); // reconst. signal

		dqsez = sr - se + sez; // pole prediction diff.

		update(2, y, _witab[i], _fitab[i], dq, sr, dqsez, state_ptr);

		return (sr << 2); // sr was of 14-bit dynamic range

	}

	public static int gsit_16_encoder(int sl, gsit_state_s state_ptr)
	{
		int sezi;
		int sez; // ACCUM
		int sei;
		int se;
		int d; // SUBTA
		int y; // MIX
		int i;
		int dq;
		int sr; // ADDB
		int dqsez; // ADDC

//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		sl >>= 2; // sl of 14-bit dynamic range

		sezi = predictor_zero(state_ptr);
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		sez = sezi >> 1;
		sei = sezi + predictor_pole(state_ptr);
//C++ TO JAVA CONVERTER WARNING: The right shift operator was not replaced by Java's logical right shift operator since the left operand was not confirmed to be of an unsigned type, but you should review whether the logical right shift operator (>>>) is more appropriate:
		se = sei >> 1; // se = estimated signal

		d = sl - se; // d = estimation diff.

		/* quantize prediction difference d */
		y = step_size(state_ptr); // quantizer step size
	tangible.RefObject<Integer> tempRef_qtab_723_16 = new tangible.RefObject<Integer>(qtab_723_16);
		i = quantize(d, y, tempRef_qtab_723_16, 1); // i = ADPCM code
		qtab_723_16 = tempRef_qtab_723_16.argValue;

			  /* Since quantize() only produces a three level output
			   * (1, 2, or 3), we must create the fourth one on our own
			   */
		if (i == 3) // i code for the zero region
		{
		  if ((d & 0x8000) == 0) // If d > 0, i=3 isn't right...
		  {
			i = 0;
		  }
		}

		dq = reconstruct(i & 2, _dqlntab[i], y); // quantized diff.

		sr = (dq < 0) ? se - (dq & 0x3FFF) : se + dq; // reconstructed signal

		dqsez = sr + sez - se; // pole prediction diff.

		update(2, y, _witab[i], _fitab[i], dq, sr, dqsez, state_ptr);

		return (i);
	}

}