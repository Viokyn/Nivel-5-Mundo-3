/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import controller.exceptions.NonexistentEntityException;
import controller.exceptions.PreexistingEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import model.Pessoa;
import model.PessoaFisica;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class PessoaFisicaJpaController implements Serializable {

    public PessoaFisicaJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(PessoaFisica pessoaFisica) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Pessoa pessoaidPessoa = pessoaFisica.getPessoaidPessoa();
            if (pessoaidPessoa != null) {
                pessoaidPessoa = em.getReference(pessoaidPessoa.getClass(), pessoaidPessoa.getIdPessoa());
                pessoaFisica.setPessoaidPessoa(pessoaidPessoa);
            }
            em.persist(pessoaFisica);
            if (pessoaidPessoa != null) {
                pessoaidPessoa.getPessoaFisicaCollection().add(pessoaFisica);
                pessoaidPessoa = em.merge(pessoaidPessoa);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findPessoaFisica(pessoaFisica.getCpf()) != null) {
                throw new PreexistingEntityException("PessoaFisica " + pessoaFisica + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(PessoaFisica pessoaFisica) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            PessoaFisica persistentPessoaFisica = em.find(PessoaFisica.class, pessoaFisica.getCpf());
            Pessoa pessoaidPessoaOld = persistentPessoaFisica.getPessoaidPessoa();
            Pessoa pessoaidPessoaNew = pessoaFisica.getPessoaidPessoa();
            if (pessoaidPessoaNew != null) {
                pessoaidPessoaNew = em.getReference(pessoaidPessoaNew.getClass(), pessoaidPessoaNew.getIdPessoa());
                pessoaFisica.setPessoaidPessoa(pessoaidPessoaNew);
            }
            pessoaFisica = em.merge(pessoaFisica);
            if (pessoaidPessoaOld != null && !pessoaidPessoaOld.equals(pessoaidPessoaNew)) {
                pessoaidPessoaOld.getPessoaFisicaCollection().remove(pessoaFisica);
                pessoaidPessoaOld = em.merge(pessoaidPessoaOld);
            }
            if (pessoaidPessoaNew != null && !pessoaidPessoaNew.equals(pessoaidPessoaOld)) {
                pessoaidPessoaNew.getPessoaFisicaCollection().add(pessoaFisica);
                pessoaidPessoaNew = em.merge(pessoaidPessoaNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = pessoaFisica.getCpf();
                if (findPessoaFisica(id) == null) {
                    throw new NonexistentEntityException("The pessoaFisica with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            PessoaFisica pessoaFisica;
            try {
                pessoaFisica = em.getReference(PessoaFisica.class, id);
                pessoaFisica.getCpf();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The pessoaFisica with id " + id + " no longer exists.", enfe);
            }
            Pessoa pessoaidPessoa = pessoaFisica.getPessoaidPessoa();
            if (pessoaidPessoa != null) {
                pessoaidPessoa.getPessoaFisicaCollection().remove(pessoaFisica);
                pessoaidPessoa = em.merge(pessoaidPessoa);
            }
            em.remove(pessoaFisica);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<PessoaFisica> findPessoaFisicaEntities() {
        return findPessoaFisicaEntities(true, -1, -1);
    }

    public List<PessoaFisica> findPessoaFisicaEntities(int maxResults, int firstResult) {
        return findPessoaFisicaEntities(false, maxResults, firstResult);
    }

    private List<PessoaFisica> findPessoaFisicaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(PessoaFisica.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public PessoaFisica findPessoaFisica(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(PessoaFisica.class, id);
        } finally {
            em.close();
        }
    }

    public int getPessoaFisicaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<PessoaFisica> rt = cq.from(PessoaFisica.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
