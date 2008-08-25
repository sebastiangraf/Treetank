/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Patent Pending.
 * 
 * NO permission to use, copy, modify, and/or distribute this software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id:sys_treetank_authentication.c 4360 2008-08-24 11:17:12Z kramis $
 */

#include "sys_treetank.h"

#define TT_OK 99
#define TT_WRITE 100
#define TT_READ_INT(X) 0;
#define TT_WRITE_INT(X,Y) ;

/* --- Function prototypes. ------------------------------------------------- */

int sys_treetank_authentication(u_int8_t, u_int8_t, u_int8_t *, u_int8_t *, u_int8_t *);
int sys_treetank_authentication_callback(struct cryptop *op);

/* --- Global variables. ---------------------------------------------------- */

static u_int64_t tt_auth_sessionId[] = {
  TT_NULL_SESSION,
  TT_NULL_SESSION,
  TT_NULL_SESSION,
  TT_NULL_SESSION,
  TT_NULL_SESSION,
  TT_NULL_SESSION,
  TT_NULL_SESSION,
  TT_NULL_SESSION };
  
static u_int8_t tt_auth_key[32];

/*
 * Perform authentication.
 */
int
sys_treetank_authentication(
  u_int8_t core,
  u_int8_t operation,
  u_int8_t *lengthPointer,
  u_int8_t *hmacPointer,
  u_int8_t *bufferPointer)
{

  /* --- Local variables. --------------------------------------------------- */
  
  int             error            = TT_SYSCALL_SUCCESS;
  struct mbuf    *packetPointer    = NULL;
  struct cryptop *operationPointer = NULL;
  u_int32_t       length           = TT_READ_INT(lengthPointer);
  
  /* --- Initialise session (if required). ---------------------------------- */
    
  if (tt_auth_sessionId[core] == TT_NULL_SESSION) {
    struct cryptoini session;  

    bzero(&session, sizeof(session));
    session.cri_alg  = TT_AUTHENTICATION_ALGORITHM;
    session.cri_klen = TT_KEY_LENGTH;
    session.cri_key  = (caddr_t) &tt_auth_key;

    if (crypto_newsession(
          &(tt_auth_sessionId[core]),
          &session,
          0) != TT_SYSCALL_SUCCESS) {
      error = TT_SYSCALL_FAILURE;
      tt_auth_sessionId[core] = TT_NULL_SESSION;
      printf("ERROR(sys_treetank_authentication.c): Could not allocate cryptoini.\n");
      goto finish;
    }
    
  }
  
  /* --- Initialise buffer. ------------------------------------------------- */
  
  packetPointer = m_gethdr(M_DONTWAIT, MT_DATA);
  if (packetPointer == NULL) {
    error = TT_SYSCALL_FAILURE;
    printf("ERROR(sys_treetank_authentication.c): Could not allocate mbuf.\n");
    goto finish;
  }
  
  packetPointer->m_pkthdr.len = 0;
  packetPointer->m_len        = 0;
    
  m_copyback(
    packetPointer,
    0,
    length,
    bufferPointer);

  /* --- Initialise crypto operation. --------------------------------------- */
  
  operationPointer = crypto_getreq(1);
  if (operationPointer == NULL) {
    error = TT_SYSCALL_FAILURE;
    printf("ERROR(sys_treetank_authentication.c): Could not allocate crypto.\n");
    goto finish;
  }

  operationPointer->crp_sid               = tt_auth_sessionId[core];
  operationPointer->crp_ilen              = length;
  operationPointer->crp_flags             = CRYPTO_F_IMBUF;
  operationPointer->crp_buf               = (caddr_t) packetPointer;
  operationPointer->crp_desc->crd_alg     = TT_AUTHENTICATION_ALGORITHM;
  operationPointer->crp_desc->crd_klen    = TT_KEY_LENGTH;
  operationPointer->crp_desc->crd_key     = (caddr_t) &tt_auth_key;
  operationPointer->crp_desc->crd_skip    = 0;
  operationPointer->crp_desc->crd_len     = length;
  operationPointer->crp_desc->crd_inject  = 0;
  operationPointer->crp_callback          = (int (*) (struct cryptop *)) sys_treetank_authentication_callback;
   
  /* --- Synchronously dispatch crypto operation. --------------------------- */
  
  crypto_dispatch(operationPointer);
  
  while (!(operationPointer->crp_flags & CRYPTO_F_DONE)) {
    error = tsleep(operationPointer, PSOCK, "sys_treetank_authentication", 0);
  }
  
  if (error != TT_SYSCALL_SUCCESS) {
    printf("ERROR(sys_treetank_authentication.c): Failed during tsleep.\n");
    goto finish;
  }
  
  if (operationPointer->crp_etype != TT_SYSCALL_SUCCESS) {
    error = operationPointer->crp_etype;
    printf("ERROR(sys_treetank_authentication.c): Failed during crypto_dispatch.\n");
    goto finish;
  }
  
  /* --- Collect result from buffer. ---------------------------------------- */
  
  packetPointer = operationPointer->crp_buf;
  
  m_copydata(
    packetPointer,
    0,
    TT_HMAC_LENGTH,
    hmacPointer);
  
  /* --- Cleanup for all conditions. ---------------------------------------- */
  
finish:

  if (packetPointer != NULL) 
    m_freem(packetPointer);
 
  if (operationPointer != NULL)
    crypto_freereq(operationPointer);
  
  return (error);
}

/*
 * Callback to retrieve result of crypto operation.
 */
int
sys_treetank_authentication_callback(struct cryptop *op)
{
  
  if (op->crp_etype == EAGAIN) {
    op->crp_flags = CRYPTO_F_IMBUF;
    return crypto_dispatch(op);
  }
  
  wakeup(op);
  return (TT_OK);
  
}