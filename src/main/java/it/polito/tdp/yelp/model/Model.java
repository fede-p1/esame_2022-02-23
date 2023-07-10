package it.polito.tdp.yelp.model;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;


import it.polito.tdp.yelp.db.YelpDao;

public class Model {
	
	YelpDao dao;
	DefaultDirectedWeightedGraph<Review,DefaultWeightedEdge> graph;
	
	public Model() {
		dao = new YelpDao();
	}
	
	public List<String> getAllCities(){
		return dao.getAllCities();
	}
	
	public List<Business> getAllBusiness(String city){
		return dao.getAllBusiness(city);
	}
	
	public DefaultDirectedWeightedGraph<Review,DefaultWeightedEdge> creaGrafo(Business b){
		
		graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		
		List<Review> vertex = new ArrayList<>(dao.getAllReviews(b.getBusinessId()));
		
		Graphs.addAllVertices(graph, vertex);
		
		for (Review r1 : graph.vertexSet())
			for (Review r2 : graph.vertexSet())
				if (!r1.equals(r2) && !(graph.containsEdge(r1, r2) || graph.containsEdge(r2, r1))){
					long days = ChronoUnit.DAYS.between(r1.getDate(), r2.getDate()); //pos se r2 after r1, neg se before
					if (days != 0) {
						if (days > 0)
							Graphs.addEdge(graph, r1, r2, days);
						else 
							Graphs.addEdge(graph, r2, r1, days);
					}
				}
		
		return graph;
		
	}
	
	private int maxUscenti;
	
	public List<Review> getReviewMax(){
		
		maxUscenti = 0;
		List<Review> result = new ArrayList<>();
		
		for (Review r : graph.vertexSet())
			if (graph.outDegreeOf(r)>maxUscenti)
				maxUscenti = graph.outDegreeOf(r);
		
		for (Review r : graph.vertexSet())
			if (graph.outDegreeOf(r)==maxUscenti)
				result.add(r);
		
		return result;
		
	}

	public int getMaxUscenti() {
		return maxUscenti;
	}
	
	private List<Review> soluzione;
	
	public List<Review> getSequenza(){
		
		soluzione = new ArrayList<>();
		List<Review> parziale = new ArrayList<>();
		
		ricorsiva(parziale, 0.0);
		
		return soluzione;
	
	}
	
	private void ricorsiva(List<Review> parziale, double lastPunteggio) {
		
		if (parziale.size() > soluzione.size()) {
			soluzione = new ArrayList<>(parziale);
		}
		
		for (Review r : graph.vertexSet()) {
			if (parziale.isEmpty()){
				parziale.add(r);
				ricorsiva(parziale,r.getStars());
				parziale.remove(parziale.size()-1);
			}
			else {
				Review last = parziale.get(parziale.size()-1);
				for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(last)) {
					Review next = graph.getEdgeTarget(edge);
					if ((!parziale.contains(next)) && (next.getStars() > lastPunteggio)) {
						parziale.add(next);
						ricorsiva(parziale,next.getStars());
						parziale.remove(parziale.size()-1);
					}
				}
			}
		}
		
		
	}
	
	public double getDays(List<Review> sequenza) {
		
		double tot = 0.0;
		
		for (int i=0; i<sequenza.size()-1; i++) {
			tot += graph.getEdgeWeight(graph.getEdge(sequenza.get(i), sequenza.get(i+1)));
		}
		return Math.abs(tot);
	}
	
	
	
	
	
}
