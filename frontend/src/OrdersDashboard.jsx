import React,{useState} from 'react';
import {ReceiptText,RefreshCw,ChartNoAxesCombined} from 'lucide-react';
const brl=v=>new Intl.NumberFormat('pt-BR',{style:'currency',currency:'BRL'}).format(Number(v)||0);
async function api(path,token){
 const res=await fetch('/api/admin'+path,{headers:{Authorization:'Bearer '+token}});
 if(!res.ok)throw new Error(res.status===403?'Acesso administrativo negado.':res.status===401?'Sessão expirada. Faça login novamente.':'Não foi possível consultar os dados: HTTP '+res.status);
 return res.json();
}
export async function getOrderData(token){
 return Promise.all([api('/metrics',token),api('/orders?limit=30',token)]);
}
export default function OrdersDashboard({metrics,orders,loading,onRefresh}){
 const [selected,setSelected]=useState(null);
 return <div className="orders-section">
  <div className="admin-top"><div><span className="eyebrow">COMMERCEAI ANALYTICS</span><h3><ChartNoAxesCombined size={21}/> Pedidos e indicadores</h3></div><button type="button" className="secondary" disabled={loading} onClick={onRefresh}><RefreshCw size={16}/> Atualizar</button></div>
  <div className="order-stats">
   <article><span>Pedidos criados</span><strong>{metrics?.ordersCreated??'—'}</strong></article>
   <article><span>Valor dos pedidos criados</span><strong>{metrics?brl(metrics.orderValueCreated):'—'}</strong></article>
   <article><span>Unidades em estoque</span><strong>{metrics?.stockUnits??'—'}</strong></article>
   <article><span>Produtos com estoque baixo</span><strong>{metrics?.lowStockProducts??'—'}</strong></article>
  </div>
  <p className="order-disclaimer">Valores de pedidos no status CREATED. O CommerceAI ainda não registra pagamentos; portanto, estes números não representam faturamento recebido.</p>
  <section className="admin-list"><h3><ReceiptText size={18}/> Pedidos recentes</h3>
   <div className="admin-tablewrap"><table><thead><tr><th>Pedido</th><th>Cliente</th><th>Data</th><th>Status</th><th>Total</th><th></th></tr></thead><tbody>{(orders||[]).map(o=><React.Fragment key={o.id}><tr><td>#{o.id}</td><td><strong>{o.customerName}</strong><small>{o.customerEmail}</small></td><td>{o.createdAt?new Date(o.createdAt).toLocaleString('pt-BR'):'—'}</td><td>{o.status}</td><td>{brl(o.total)}</td><td><button className="secondary" type="button" onClick={()=>setSelected(selected===o.id?null:o.id)}>{selected===o.id?'Ocultar':'Itens'}</button></td></tr>{selected===o.id&&<tr><td colSpan={6}><div className="order-items">{o.items.map((item,i)=><div key={i}>{item.quantity} × {item.productName} — {brl(item.unitPrice)} por unidade</div>)}</div></td></tr>}</React.Fragment>)}</tbody></table></div>
   {!orders?.length&&<p>Nenhum pedido registrado.</p>}
  </section>
 </div>;
}
