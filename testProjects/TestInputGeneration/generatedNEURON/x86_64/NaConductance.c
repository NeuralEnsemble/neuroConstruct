/* Created by Language version: 6.0.2 */
/* NOT VECTORIZED */
#include <stdio.h>
#include <math.h>
#include "scoplib.h"
#undef PI
 
#include "md1redef.h"
#include "section.h"
#include "nrnoc_ml.h"
#include "md2redef.h"

#if METHOD3
extern int _method3;
#endif

#undef exp
#define exp hoc_Exp
extern double hoc_Exp();
 	/*SUPPRESS 761*/
	/*SUPPRESS 762*/
	/*SUPPRESS 763*/
	/*SUPPRESS 765*/
	 extern double *getarg();
 static double *_p; static Datum *_ppvar;
 
#define delta_t dt
#define gmax _p[0]
#define gion _p[1]
#define minf _p[2]
#define mtau _p[3]
#define hinf _p[4]
#define htau _p[5]
#define m _p[6]
#define h _p[7]
#define ena _p[8]
#define ina _p[9]
#define Dm _p[10]
#define Dh _p[11]
#define _g _p[12]
#define _ion_ena	*_ppvar[0].pval
#define _ion_ina	*_ppvar[1].pval
#define _ion_dinadv	*_ppvar[2].pval
 
#if MAC
#if !defined(v)
#define v _mlhv
#endif
#if !defined(h)
#define h _mlhh
#endif
#endif
 static int hoc_nrnpointerindex =  -1;
 /* external NEURON variables */
 extern double celsius;
 extern double dt;
 extern double t;
 /* declaration of user functions */
 static int _hoc_rates();
 static int _hoc_vtrap();
 static int _mechtype;
extern int nrn_get_mechtype();
 static _hoc_setdata() {
 Prop *_prop, *hoc_getdata_range();
 _prop = hoc_getdata_range("NaConductance");
 _p = _prop->param; _ppvar = _prop->dparam;
 ret(1.);
}
 /* connect user functions to hoc names */
 static IntFunc hoc_intfunc[] = {
 "setdata_NaConductance", _hoc_setdata,
 "rates_NaConductance", _hoc_rates,
 "vtrap_NaConductance", _hoc_vtrap,
 0, 0
};
#define vtrap vtrap_NaConductance
 extern double vtrap();
 /* declare global and static user variables */
#define usetable usetable_NaConductance
 double usetable = 1;
 /* some parameters have upper and lower limits */
 static HocParmLimits _hoc_parm_limits[] = {
 "usetable_NaConductance", 0, 1,
 0,0,0
};
 static HocParmUnits _hoc_parm_units[] = {
 "gmax_NaConductance", "S/cm2",
 "gion_NaConductance", "S/cm2",
 "mtau_NaConductance", "ms",
 "htau_NaConductance", "ms",
 0,0
};
 static double h0 = 0;
 static double m0 = 0;
 static double v = 0;
 /* connect global user variables to hoc */
 static DoubScal hoc_scdoub[] = {
 "usetable_NaConductance", &usetable,
 0,0
};
 static DoubVec hoc_vdoub[] = {
 0,0,0
};
 static double _sav_indep;
 static nrn_alloc(), nrn_init(), nrn_state();
 static nrn_cur(), nrn_jacob();
 
static int _ode_count(), _ode_map(), _ode_spec(), _ode_matsol();
extern int nrn_cvode_;
 
#define _cvode_ieq _ppvar[3]._i
 /* connect range variables in _p that hoc is supposed to know about */
 static char *_mechanism[] = {
 "6.0.2",
"NaConductance",
 "gmax_NaConductance",
 0,
 "gion_NaConductance",
 "minf_NaConductance",
 "mtau_NaConductance",
 "hinf_NaConductance",
 "htau_NaConductance",
 0,
 "m_NaConductance",
 "h_NaConductance",
 0,
 0};
 static Symbol* _na_sym;
 
static nrn_alloc(_prop)
	Prop *_prop;
{
	Prop *prop_ion, *need_memb();
	double *_p; Datum *_ppvar;
 	_p = nrn_prop_data_alloc(_mechtype, 13);
 	/*initialize range parameters*/
 	gmax = 0.12;
 	_prop->param = _p;
 	_prop->param_size = 13;
 	_ppvar = nrn_prop_datum_alloc(_mechtype, 4);
 	_prop->dparam = _ppvar;
 	/*connect ionic variables to this model*/
 prop_ion = need_memb(_na_sym);
 nrn_promote(prop_ion, 0, 1);
 	_ppvar[0].pval = &prop_ion->param[0]; /* ena */
 	_ppvar[1].pval = &prop_ion->param[3]; /* ina */
 	_ppvar[2].pval = &prop_ion->param[4]; /* _ion_dinadv */
 
}
 static _initlists();
  /* some states have an absolute tolerance */
 static Symbol** _atollist;
 static HocStateTolerance _hoc_state_tol[] = {
 0,0
};
 _NaConductance_reg() {
	int _vectorized = 0;
  _initlists();
 	ion_reg("na", 1.0);
 	_na_sym = hoc_lookup("na_ion");
 	register_mech(_mechanism, nrn_alloc,nrn_cur, nrn_jacob, nrn_state, nrn_init, hoc_nrnpointerindex, _vectorized);
 _mechtype = nrn_get_mechtype(_mechanism[1]);
  hoc_register_dparam_size(_mechtype, 4);
 	hoc_register_cvode(_mechtype, _ode_count, _ode_map, _ode_spec, _ode_matsol);
 	hoc_register_tolerance(_mechtype, _hoc_state_tol, &_atollist);
 	hoc_register_var(hoc_scdoub, hoc_vdoub, hoc_intfunc);
 	ivoc_help("help ?1 NaConductance /home/matteo/neuroConstruct/testProjects/TestInputGeneration/generatedNEURON/x86_64/NaConductance.mod\n");
 hoc_register_limits(_mechtype, _hoc_parm_limits);
 hoc_register_units(_mechtype, _hoc_parm_units);
 }
 static double *_t_minf;
 static double *_t_mtau;
 static double *_t_hinf;
 static double *_t_htau;
static int _reset;
static char *modelname = "Channel: NaConductance";

static int error;
static int _ninits = 0;
static int _match_recurse=1;
static _modl_cleanup(){ _match_recurse=1;}
static _f_rates();
static rates();
 
static int _ode_spec1(), _ode_matsol1();
 static _check_rates();
 static _n_rates();
 static int _slist1[2], _dlist1[2];
 static int states();
 
/*CVODE*/
 static int _ode_spec1 () {_reset=0;
 {
   rates (  v ) ;
   Dm = ( minf - m ) / mtau ;
   Dh = ( hinf - h ) / htau ;
   }
 return _reset;
}
 static int _ode_matsol1() {
 rates (  v ) ;
 Dm = Dm  / (1. - dt*( ( ( ( - 1.0 ) ) ) / mtau )) ;
 Dh = Dh  / (1. - dt*( ( ( ( - 1.0 ) ) ) / htau )) ;
}
 /*END CVODE*/
 static int states () {_reset=0;
 {
   rates (  v ) ;
    m = m + (1. - exp(dt*(( ( ( - 1.0 ) ) ) / mtau)))*(- ( ( ( minf ) ) / mtau ) / ( ( ( ( - 1.0) ) ) / mtau ) - m) ;
    h = h + (1. - exp(dt*(( ( ( - 1.0 ) ) ) / htau)))*(- ( ( ( hinf ) ) / htau ) / ( ( ( ( - 1.0) ) ) / htau ) - h) ;
   }
  return 0;
}
 static double _mfac_rates, _tmin_rates;
 static _check_rates() {
  static int _maktable=1; int _i, _j, _ix = 0;
  double _xi, _tmax;
  static double _sav_celsius;
  if (!usetable) {return;}
  if (_sav_celsius != celsius) { _maktable = 1;}
  if (_maktable) { double _x, _dx; _maktable=0;
   _tmin_rates =  - 100.0 ;
   _tmax =  100.0 ;
   _dx = (_tmax - _tmin_rates)/400.; _mfac_rates = 1./_dx;
   for (_i=0, _x=_tmin_rates; _i < 401; _x += _dx, _i++) {
    _f_rates(_x);
    _t_minf[_i] = minf;
    _t_mtau[_i] = mtau;
    _t_hinf[_i] = hinf;
    _t_htau[_i] = htau;
   }
   _sav_celsius = celsius;
  }
 }

 static rates(_lv) double _lv;{ _check_rates();
 _n_rates(_lv);
 return;
 }

 static _n_rates(_lv) double _lv;{ int _i, _j;
 double _xi, _theta;
 if (!usetable) {
 _f_rates(_lv); return; 
}
 _xi = _mfac_rates * (_lv - _tmin_rates);
 _i = (int) _xi;
 if (_xi <= 0.) {
 minf = _t_minf[0];
 mtau = _t_mtau[0];
 hinf = _t_hinf[0];
 htau = _t_htau[0];
 return; }
 if (_i >= 400) {
 minf = _t_minf[400];
 mtau = _t_mtau[400];
 hinf = _t_hinf[400];
 htau = _t_htau[400];
 return; }
 _theta = _xi - (double)_i;
 minf = _t_minf[_i] + _theta*(_t_minf[_i+1] - _t_minf[_i]);
 mtau = _t_mtau[_i] + _theta*(_t_mtau[_i+1] - _t_mtau[_i]);
 hinf = _t_hinf[_i] + _theta*(_t_hinf[_i+1] - _t_hinf[_i]);
 htau = _t_htau[_i] + _theta*(_t_htau[_i+1] - _t_htau[_i]);
 }

 
static int  _f_rates (  _lv )  
	double _lv ;
 {
   double _lalpha , _lbeta , _ltau , _linf , _lgamma , _lzeta , _ltemp_adj_m , _lA_alpha_m , _lk_alpha_m , _ld_alpha_m , _lA_beta_m , _lk_beta_m , _ld_beta_m , _ltemp_adj_h , _lA_alpha_h , _lk_alpha_h , _ld_alpha_h , _lA_beta_h , _lk_beta_h , _ld_beta_h ;
  _ltemp_adj_m = 1.0 ;
   _ltemp_adj_h = 1.0 ;
   _lA_alpha_m = 1.0 ;
   _lk_alpha_m = 0.1 ;
   _ld_alpha_m = - 40.0 ;
   _lalpha = _lA_alpha_m * vtrap (  ( _lv - _ld_alpha_m ) , ( 1.0 / _lk_alpha_m ) ) ;
   _lA_beta_m = 4.0 ;
   _lk_beta_m = - 0.0555555555 ;
   _ld_beta_m = - 65.0 ;
   _lbeta = _lA_beta_m * exp ( ( _lv - _ld_beta_m ) * _lk_beta_m ) ;
   mtau = 1.0 / ( _ltemp_adj_m * ( _lalpha + _lbeta ) ) ;
   minf = _lalpha / ( _lalpha + _lbeta ) ;
   _lA_alpha_h = 0.07 ;
   _lk_alpha_h = - 0.05 ;
   _ld_alpha_h = - 65.0 ;
   _lalpha = _lA_alpha_h * exp ( ( _lv - _ld_alpha_h ) * _lk_alpha_h ) ;
   _lA_beta_h = 1.0 ;
   _lk_beta_h = - 0.1 ;
   _ld_beta_h = - 35.0 ;
   _lbeta = _lA_beta_h / ( exp ( ( _lv - _ld_beta_h ) * _lk_beta_h ) + 1.0 ) ;
   htau = 1.0 / ( _ltemp_adj_h * ( _lalpha + _lbeta ) ) ;
   hinf = _lalpha / ( _lalpha + _lbeta ) ;
    return 0; }
 static int _hoc_rates() {
 double _r;
  _r = 1.;
 rates (  *getarg(1) ) ;
 ret(_r);
}
 
double vtrap (  _lVminV0 , _lB )  
	double _lVminV0 , _lB ;
 {
   double _lvtrap;
 if ( fabs ( _lVminV0 / _lB ) < 1e-6 ) {
     _lvtrap = ( 1.0 + _lVminV0 / _lB / 2.0 ) ;
     }
   else {
     _lvtrap = ( _lVminV0 / _lB ) / ( 1.0 - exp ( ( - 1.0 * _lVminV0 ) / _lB ) ) ;
     }
   
return _lvtrap;
 }
 static int _hoc_vtrap() {
 double _r;
 _r =  vtrap (  *getarg(1) , *getarg(2) ) ;
 ret(_r);
}
 
static int _ode_count(_type) int _type;{ return 2;}
 
static int _ode_spec(_nd, _pp, _ppd) Node* _nd; double* _pp; Datum* _ppd; {
	_p = _pp; _ppvar = _ppd; v = NODEV(_nd);
  ena = _ion_ena;
  _ode_spec1();
  }
 
static int _ode_map(_ieq, _pv, _pvdot, _pp, _ppd, _atol, _type) int _ieq, _type; double** _pv, **_pvdot, *_pp, *_atol; Datum* _ppd; {
	int _i; _p = _pp; _ppvar = _ppd;
	_cvode_ieq = _ieq;
	for (_i=0; _i < 2; ++_i) {
		_pv[_i] = _pp + _slist1[_i];  _pvdot[_i] = _pp + _dlist1[_i];
		_cvode_abstol(_atollist, _atol, _i);
	}
 }
 
static int _ode_matsol(_nd, _pp, _ppd) Node* _nd; double* _pp; Datum* _ppd; {
	_p = _pp; _ppvar = _ppd; v = NODEV(_nd);
  ena = _ion_ena;
 _ode_matsol1();
 }

static initmodel() {
  int _i; double _save;_ninits++;
 _save = t;
 t = 0.0;
{
  h = h0;
  m = m0;
 {
   ena = 50.0 ;
   rates (  v ) ;
   m = minf ;
   h = hinf ;
   }
  _sav_indep = t; t = _save;

}
}

static nrn_init(_ml, _type) _Memb_list* _ml; int _type;{
Node *_nd; double _v; int* _ni; int _iml, _cntml;
#if CACHEVEC
    _ni = _ml->_nodeindices;
#endif
_cntml = _ml->_nodecount;
for (_iml = 0; _iml < _cntml; ++_iml) {
 _p = _ml->_data[_iml]; _ppvar = _ml->_pdata[_iml];
#if CACHEVEC
  if (use_cachevec) {
    _v = VEC_V(_ni[_iml]);
  }else
#endif
  {
    _nd = _ml->_nodelist[_iml];
    _v = NODEV(_nd);
  }
 v = _v;
  ena = _ion_ena;
 initmodel();
 }}

static double _nrn_current(_v) double _v;{double _current=0.;v=_v;{ {
   gion = gmax * ( pow( ( 1.0 * m ) , 3.0 ) ) * ( pow( ( 1.0 * h ) , 1.0 ) ) ;
   ina = gion * ( v - ena ) ;
   }
 _current += ina;

} return _current;
}

static nrn_cur(_ml, _type) _Memb_list* _ml; int _type;{
Node *_nd; int* _ni; double _rhs, _v; int _iml, _cntml;
#if CACHEVEC
    _ni = _ml->_nodeindices;
#endif
_cntml = _ml->_nodecount;
for (_iml = 0; _iml < _cntml; ++_iml) {
 _p = _ml->_data[_iml]; _ppvar = _ml->_pdata[_iml];
#if CACHEVEC
  if (use_cachevec) {
    _v = VEC_V(_ni[_iml]);
  }else
#endif
  {
    _nd = _ml->_nodelist[_iml];
    _v = NODEV(_nd);
  }
  ena = _ion_ena;
 _g = _nrn_current(_v + .001);
 	{ static double _dina;
  _dina = ina;
 _rhs = _nrn_current(_v);
  _ion_dinadv += (_dina - ina)/.001 ;
 	}
 _g = (_g - _rhs)/.001;
  _ion_ina += ina ;
#if CACHEVEC
  if (use_cachevec) {
	VEC_RHS(_ni[_iml]) -= _rhs;
  }else
#endif
  {
	NODERHS(_nd) -= _rhs;
  }
 
}}

static nrn_jacob(_ml, _type) _Memb_list* _ml; int _type;{
Node *_nd; int* _ni; int _iml, _cntml;
#if CACHEVEC
    _ni = _ml->_nodeindices;
#endif
_cntml = _ml->_nodecount;
for (_iml = 0; _iml < _cntml; ++_iml) {
 _p = _ml->_data[_iml];
#if CACHEVEC
  if (use_cachevec) {
	VEC_D(_ni[_iml]) += _g;
  }else
#endif
  {
     _nd = _ml->_nodelist[_iml];
	NODED(_nd) += _g;
  }
 
}}

static nrn_state(_ml, _type) _Memb_list* _ml; int _type;{
 double _break, _save;
Node *_nd; double _v; int* _ni; int _iml, _cntml;
#if CACHEVEC
    _ni = _ml->_nodeindices;
#endif
_cntml = _ml->_nodecount;
for (_iml = 0; _iml < _cntml; ++_iml) {
 _p = _ml->_data[_iml]; _ppvar = _ml->_pdata[_iml];
 _nd = _ml->_nodelist[_iml];
#if CACHEVEC
  if (use_cachevec) {
    _v = VEC_V(_ni[_iml]);
  }else
#endif
  {
    _nd = _ml->_nodelist[_iml];
    _v = NODEV(_nd);
  }
 _break = t + .5*dt; _save = t; delta_t = dt;
 v=_v;
{
  ena = _ion_ena;
 { {
 for (; t < _break; t += delta_t) {
 error =  states();
 if(error){fprintf(stderr,"at line 148 in file NaConductance.mod:\n    \n"); nrn_complain(_p); abort_run(error);}
 
}}
 t = _save;
 } }}

}

static terminal(){}

static _initlists() {
 int _i; static int _first = 1;
  if (!_first) return;
 _slist1[0] = &(m) - _p;  _dlist1[0] = &(Dm) - _p;
 _slist1[1] = &(h) - _p;  _dlist1[1] = &(Dh) - _p;
   _t_minf = makevector(401*sizeof(double));
   _t_mtau = makevector(401*sizeof(double));
   _t_hinf = makevector(401*sizeof(double));
   _t_htau = makevector(401*sizeof(double));
_first = 0;
}
