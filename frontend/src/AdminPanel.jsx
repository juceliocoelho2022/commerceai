import React,{useState} from 'react';
import {ShieldCheck,LogIn,LogOut,Plus,Save,Trash2,Pencil,RefreshCw} from 'lucide-react';

const blank={name:'',category:'Eletrônicos',description:'',price:'',stock:'0',imageUrl:''};
const brl=n=>new Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL'}).format(n);
async function request(path,token,method='GET',body){
 const response=await fetch('/api'+path,{
  method,
  headers:{...(body?{'Content-Type':'application/json'}:{}),...(token?{Authorization:'Bearer '+token}:{})},
  ...(body?{body:JSON.stringify(body)}:{})
 });
 if(!response.ok){
  const data=await response.json().catch(()=>({}));
  throw new Error(response.status===401?'Sessão inválida ou expirada. Entre novamente.':response.status===403?'Acesso negado. Necessário perfil ADMIN.':(data.detail||data.message||('Falha HTTP '+response.status)));
 }
 return response.status===204?null:response.json();
}
export default function AdminPanel({onCatalogChange}){
 const [credentials,setCredentials]=useState({email:'',password:''});
 const [token,setToken]=useState('');
 const [items,setItems]=useState([]);
 const [form,setForm]=useState(blank);
 const [editing,setEditing]=useState(null);
 const [loading,setLoading]=useState(false);
 const [notice,setNotice]=useState('');
 async function refresh(jwt=token){setItems(await request('/admin/products',jwt));}
 async function login(event){
  event.preventDefault();setLoading(true);setNotice('');
  try{
   const result=await request('/auth/login','', 'POST',credentials);
   await refresh(result.accessToken);
   setToken(result.accessToken);setCredentials(v=>({...v,password:''}));
  }catch(error){setToken('');setItems([]);setNotice(error.message)}
  finally{setLoading(false)}
 }
 function logout(){setToken('');setItems([]);setEditing(null);setForm(blank);setNotice('Sessão encerrada.')}
 function edit(product){setEditing(product.id);setForm({name:product.name,category:product.category,description:product.description,price:String(product.price),stock:String(product.stock),imageUrl:product.imageUrl||''});}
 async function submit(event){
  event.preventDefault();setLoading(true);setNotice('');
  try{
   const payload={...form,name:form.name.trim(),category:form.category.trim(),description:form.description.trim(),price:Number(form.price),stock:Number(form.stock),imageUrl:form.imageUrl.trim()||null};
   if(!Number.isFinite(payload.price)||payload.price<=0||!Number.isInteger(payload.stock)||payload.stock<0)throw Error('Informe preço positivo e estoque inteiro não negativo.');
   await request(editing?'/admin/products/'+editing:'/admin/products',token,editing?'PUT':'POST',payload);
   setForm(blank);setEditing(null);await refresh();await onCatalogChange();
   setNotice(editing?'Produto atualizado.':'Produto cadastrado.');
  }catch(error){setNotice(error.message)}finally{setLoading(false)}
 }
 async function remove(product){
  if(!window.confirm('Excluir o produto '+product.name+'? Produtos com pedidos vinculados não podem ser excluídos.'))return;
  setLoading(true);setNotice('');
  try{await request('/admin/products/'+product.id,token,'DELETE');await refresh();await onCatalogChange();setNotice('Produto excluído.')}
  catch(error){setNotice(error.message)}finally{setLoading(false)}
 }
 return <section className="admin">
  <div className="admin-top"><div><span className="eyebrow">ÁREA RESTRITA</span><h2><ShieldCheck size={24}/> Gestão de produtos</h2></div>{token&&<button type="button" className="secondary" onClick={logout}><LogOut size={16}/> Sair</button>}</div>
  {notice&&<p className="admin-alert" role="status">{notice}</p>}
  {!token?<form className="admin-login" onSubmit={login}><h3><LogIn size={20}/> Login administrativo</h3><p>Entre com sua conta ADMIN. A autenticação é verificada no servidor; contas CUSTOMER não têm acesso.</p>
   <label>E-mail<input type="email" required autoComplete="username" value={credentials.email} onChange={e=>setCredentials(v=>({...v,email:e.target.value}))}/></label>
   <label>Senha<input type="password" required autoComplete="current-password" value={credentials.password} onChange={e=>setCredentials(v=>({...v,password:e.target.value}))}/></label>
   <button className="primary" disabled={loading} type="submit">{loading?'Validando...':'Entrar no painel'} <LogIn size={16}/></button>
  </form>:<>
   <div className="admin-summary"><div><span>Produtos cadastrados</span><strong>{items.length}</strong></div><div><span>Estoque baixo (≤10)</span><strong>{items.filter(x=>x.stock<=10).length}</strong></div><button type="button" className="secondary" disabled={loading} onClick={()=>refresh().catch(e=>setNotice(e.message))}><RefreshCw size={16}/> Atualizar</button></div>
   <div className="admin-layout"><form className="admin-editor" onSubmit={submit}><h3>{editing?'Editar produto #'+editing:'Cadastrar produto'}</h3>
    <label>Nome<input required maxLength={150} value={form.name} onChange={e=>setForm(v=>({...v,name:e.target.value}))}/></label>
    <label>Categoria<input required maxLength={70} value={form.category} onChange={e=>setForm(v=>({...v,category:e.target.value}))}/></label>
    <label>Descrição<textarea required maxLength={500} value={form.description} onChange={e=>setForm(v=>({...v,description:e.target.value}))}/></label>
    <div className="admin-fields"><label>Preço (R$)<input type="number" min="0.01" step="0.01" required value={form.price} onChange={e=>setForm(v=>({...v,price:e.target.value}))}/></label>
    <label>Estoque<input type="number" min="0" step="1" required value={form.stock} onChange={e=>setForm(v=>({...v,stock:e.target.value}))}/></label></div>
    <label>URL da imagem (opcional)<input type="url" maxLength={500} value={form.imageUrl} onChange={e=>setForm(v=>({...v,imageUrl:e.target.value}))}/></label>
    <button className="primary" type="submit" disabled={loading}>{editing?<Save size={16}/>:<Plus size={16}/>} {loading?'Salvando...':editing?'Salvar alterações':'Cadastrar produto'}</button>
    {editing&&<button type="button" className="secondary" onClick={()=>{setEditing(null);setForm(blank)}}>Cancelar edição</button>}
   </form><div className="admin-list"><h3>Catálogo administrável</h3><div className="admin-tablewrap"><table><thead><tr><th>Produto</th><th>Preço</th><th>Estoque</th><th>Ações</th></tr></thead><tbody>{items.map(product=><tr key={product.id}><td><strong>{product.name}</strong><small>{product.category}</small></td><td>{brl(product.price)}</td><td>{product.stock}</td><td><div className="admin-actions"><button type="button" title="Editar produto" onClick={()=>edit(product)}><Pencil size={16}/></button><button type="button" title="Excluir produto" disabled={loading} onClick={()=>remove(product)}><Trash2 size={16}/></button></div></td></tr>)}</tbody></table></div>{!items.length&&<p>Nenhum produto cadastrado.</p>}</div></div>
  </>}
 </section>;
}
