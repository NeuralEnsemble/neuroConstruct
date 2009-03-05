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
#define minFrequency _p[0]
#define noise _p[1]
#define dur _p[2]
#define del _p[3]
#define event _p[4]
#define on _p[5]
#define end _p[6]
#define _tsav _p[7]
#define _nd_area  *_ppvar[0].pval
 
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
 extern double dt;
 extern double t;
 /* declaration of user functions */
 static double _hoc_event_time();
 static double _hoc_getFrequency();
 static double _hoc_init_sequence();
 static double _hoc_invl();
 static double _hoc_seed();
 static int _mechtype;
extern int nrn_get_mechtype();
 extern Prop* nrn_point_prop_;
 static int _pointtype;
 static void* _hoc_create_pnt(_ho) Object* _ho; { void* create_point_process();
 return create_point_process(_pointtype, _ho);
}
 static void _hoc_destroy_pnt();
 static double _hoc_loc_pnt(_vptr) void* _vptr; {double loc_point_process();
 return loc_point_process(_pointtype, _vptr);
}
 static double _hoc_has_loc(_vptr) void* _vptr; {double has_loc_point();
 return has_loc_point(_vptr);
}
 static double _hoc_get_loc_pnt(_vptr)void* _vptr; {
 double get_loc_point_process(); return (get_loc_point_process(_vptr));
}
 static _hoc_setdata(_vptr) void* _vptr; { Prop* _prop;
 _prop = ((Point_process*)_vptr)->_prop;
 _p = _prop->param; _ppvar = _prop->dparam;
 }
 /* connect user functions to hoc names */
 static IntFunc hoc_intfunc[] = {
 0,0
};
 static struct Member_func {
	char* _name; double (*_member)();} _member_func[] = {
 "loc", _hoc_loc_pnt,
 "has_loc", _hoc_has_loc,
 "get_loc", _hoc_get_loc_pnt,
 "event_time", _hoc_event_time,
 "getFrequency", _hoc_getFrequency,
 "init_sequence", _hoc_init_sequence,
 "invl", _hoc_invl,
 "seed", _hoc_seed,
 0, 0
};
#define getFrequency getFrequency_NetStimVariable
#define invl invl_NetStimVariable
 extern double getFrequency();
 extern double invl();
 /* declare global and static user variables */
 /* some parameters have upper and lower limits */
 static HocParmLimits _hoc_parm_limits[] = {
 "minFrequency", 1e-09, 1e+09,
 "noise", 0, 1,
 0,0,0
};
 static HocParmUnits _hoc_parm_units[] = {
 "minFrequency", "1/ms",
 "dur", "ms",
 "del", "ms",
 0,0
};
 static double v = 0;
 /* connect global user variables to hoc */
 static DoubScal hoc_scdoub[] = {
 0,0
};
 static DoubVec hoc_vdoub[] = {
 0,0,0
};
 static double _sav_indep;
 static nrn_alloc(), nrn_init(), nrn_state();
 static void _hoc_destroy_pnt(_vptr) void* _vptr; {
   destroy_point_process(_vptr);
}
 /* connect range variables in _p that hoc is supposed to know about */
 static char *_mechanism[] = {
 "6.0.2",
"NetStimVariable",
 "minFrequency",
 "noise",
 "dur",
 "del",
 0,
 0,
 0,
 0};
 
static nrn_alloc(_prop)
	Prop *_prop;
{
	Prop *prop_ion, *need_memb();
	double *_p; Datum *_ppvar;
  if (nrn_point_prop_) {
	_p = nrn_point_prop_->param;
	_ppvar = nrn_point_prop_->dparam;
 }else{
 	_p = nrn_prop_data_alloc(_mechtype, 8);
 	/*initialize range parameters*/
 	minFrequency = 0.0005;
 	noise = 0;
 	dur = 800;
 	del = 100;
  }
 	_prop->param = _p;
 	_prop->param_size = 8;
  if (!nrn_point_prop_) {
 	_ppvar = nrn_prop_datum_alloc(_mechtype, 3);
  }
 	_prop->dparam = _ppvar;
 	/*connect ionic variables to this model*/
 
}
 static _initlists();
 
#define _tqitem &(_ppvar[2]._pvoid)
 static _net_receive();
 typedef (*_Pfrv)();
 extern _Pfrv* pnt_receive;
 extern short* pnt_receive_size;
 _NetStimVariable_reg() {
	int _vectorized = 0;
  _initlists();
 	_pointtype = point_register_mech(_mechanism,
	 nrn_alloc,0, 0, 0, nrn_init,
	 hoc_nrnpointerindex,
	 _hoc_create_pnt, _hoc_destroy_pnt, _member_func,
	 _vectorized);
 _mechtype = nrn_get_mechtype(_mechanism[1]);
  hoc_register_dparam_size(_mechtype, 3);
 add_nrn_artcell(_mechtype, 2);
 add_nrn_has_net_event(_mechtype);
 pnt_receive[_mechtype] = _net_receive;
 pnt_receive_size[_mechtype] = 1;
 	hoc_register_var(hoc_scdoub, hoc_vdoub, hoc_intfunc);
 	ivoc_help("help ?1 NetStimVariable /home/matteo/neuroConstruct/testProjects/TestInputGeneration/generatedNEURON/x86_64/NetStimVariable.mod\n");
 hoc_register_limits(_mechtype, _hoc_parm_limits);
 hoc_register_units(_mechtype, _hoc_parm_units);
 }
static int _reset;
static char *modelname = "";

static int error;
static int _ninits = 0;
static int _match_recurse=1;
static _modl_cleanup(){ _match_recurse=1;}
static event_time();
static init_sequence();
static seed();
 
static int  seed (  _lx )  
	double _lx ;
 {
   set_seed ( _lx ) ;
    return 0; }
 static double _hoc_seed(_vptr) void* _vptr; {
 double _r;
 	_hoc_setdata(_vptr);
 _r = 1.;
 seed (  *getarg(1) ) ;
 return(_r);
}
 
double getFrequency (  )  {
   double _lgetFrequency;
 double _levalFreq ;
 _levalFreq = 0.05 + 0.04 * cos ( 2.0 * 3.14159265 * t / 400.0 ) ;
   if ( _levalFreq < minFrequency ) {
     _lgetFrequency = minFrequency ;
     }
   else {
     _lgetFrequency = _levalFreq ;
     }
   
return _lgetFrequency;
 }
 static double _hoc_getFrequency(_vptr) void* _vptr; {
 double _r;
 	_hoc_setdata(_vptr);
 _r =  getFrequency (  ) ;
 return(_r);
}
 
static int  init_sequence (  _lt )  
	double _lt ;
 {
   if ( getFrequency (  ) > 0.0 ) {
     on = 1.0 ;
     event = _lt ;
     end = del + dur ;
     }
    return 0; }
 static double _hoc_init_sequence(_vptr) void* _vptr; {
 double _r;
 	_hoc_setdata(_vptr);
 _r = 1.;
 init_sequence (  *getarg(1) ) ;
 return(_r);
}
 
double invl (  _lmean )  
	double _lmean ;
 {
   double _linvl;
 if ( _lmean <= 0. ) {
     _lmean = .01 ;
     }
   if ( noise  == 0.0 ) {
     _linvl = _lmean ;
     }
   else {
     _linvl = ( 1. - noise ) * _lmean + noise * _lmean * exprand ( 1.0 ) ;
     }
   
return _linvl;
 }
 static double _hoc_invl(_vptr) void* _vptr; {
 double _r;
 	_hoc_setdata(_vptr);
 _r =  invl (  *getarg(1) ) ;
 return(_r);
}
 
static int  event_time (  )  {
   if ( getFrequency (  ) > 0.0 ) {
     event = event + invl (  1.0 / getFrequency (  ) ) ;
     }
   if ( event > end ) {
     on = 0.0 ;
     }
    return 0; }
 static double _hoc_event_time(_vptr) void* _vptr; {
 double _r;
 	_hoc_setdata(_vptr);
 _r = 1.;
 event_time (  ) ;
 return(_r);
}
 
static _net_receive (_pnt, _args, _lflag) Point_process* _pnt; double* _args; double _lflag; 
{ _p = _pnt->_prop->param; _ppvar = _pnt->_prop->dparam;
  if (_tsav > t){ extern char* hoc_object_name(); hoc_execerror(hoc_object_name(_pnt->ob), ":Event arrived out of order. Must call ParallelContext.set_maxstep AFTER assigning minimum NetCon.delay");}
 _tsav = t;   if (_lflag == 1. ) {*(_tqitem) = 0;}
 {
   if ( _lflag  == 0.0 ) {
     if ( _args[0] > 0.0  && on  == 0.0 ) {
       init_sequence (  t ) ;
       artcell_net_send ( _tqitem, _args, _pnt, 0.0 , 1.0 ) ;
       }
     else if ( _args[0] < 0.0  && on  == 1.0 ) {
       on = 0.0 ;
       }
     }
   if ( _lflag  == 3.0 ) {
     if ( on  == 0.0 ) {
       init_sequence (  t ) ;
       artcell_net_send ( _tqitem, _args, _pnt, 0.0 , 1.0 ) ;
       }
     }
   if ( _lflag  == 1.0  && on  == 1.0 ) {
     net_event ( _pnt, t ) ;
     event_time (  ) ;
     if ( on  == 1.0 ) {
       artcell_net_send ( _tqitem, _args, _pnt, event - t , 1.0 ) ;
       }
     }
   } }

static initmodel() {
  int _i; double _save;_ninits++;
{
 {
   double _linterval ;
 on = 0.0 ;
   if ( noise < 0.0 ) {
     noise = 0.0 ;
     }
   if ( noise > 1.0 ) {
     noise = 1.0 ;
     }
   if ( del >= 0.0 ) {
     _linterval = 1.0 / getFrequency (  ) ;
     event = del + invl (  _linterval ) - _linterval * ( 1. - noise ) ;
     if ( event < 0.0 ) {
       event = 0.0 ;
       }
     artcell_net_send ( _tqitem, (double*)0, _ppvar[1]._pvoid, event , 3.0 ) ;
     }
   }

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
 _tsav = -1e20;
 initmodel();
}}

static double _nrn_current(_v) double _v;{double _current=0.;v=_v;{
} return _current;
}

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
 _break = t + .5*dt; _save = t; delta_t = dt;
 v=_v;
{
}}

}

static terminal(){}

static _initlists() {
 int _i; static int _first = 1;
  if (!_first) return;
_first = 0;
}
