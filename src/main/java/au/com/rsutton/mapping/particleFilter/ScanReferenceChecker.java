package au.com.rsutton.mapping.particleFilter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The idea this revolves around is that when a scan is taken, the particle
 * filter can only tell us approximately where we are.
 * 
 * Over time though, the particles in the particleFilter will get resampled and
 * particles that were incorrectly positioned at the time of the scan will be
 * removed. This will leave a set of Poses which have a common ancestor where
 * the scan was taken.
 * 
 * By adding the location the particle was at when the scan was taken, we can at
 * a future point when all the particles have this common ancestor be certain
 * that we know where we were when the scan was taken
 * 
 * 
 * 
 * this class is intended to iterate a set of particles looking for a scan
 * (ParticleFilterObservationSet) that has a single (or sufficiently small
 * variance) Pose.
 * 
 * Upon locating a scan the ObservationListener will be notified, and
 * subsequently the scan removed from all particles
 * 
 * @author rsutton
 *
 */
public class ScanReferenceChecker
{

	void check(List<Particle> particles, ObservationListener listener)
	{
		Map<ParticleFilterObservationSet, Set<Pose>> scansReferences = new HashMap<>();

		// iterate the particles and accumulate scans and the associated unique
		// poses

		for (Particle particle : particles)
		{
			List<ScanReference> references = particle.getScanReferences();
			for (ScanReference reference : references)
			{
				ParticleFilterObservationSet scan = reference.getScan();

				Set<Pose> poses = getScanReference(scan, scansReferences);
				poses.add(reference.getScanOrigin());
			}
		}

		for (Map.Entry<ParticleFilterObservationSet, Set<Pose>> entry : scansReferences.entrySet())
		{
			// determine the variance of the known Poses for the observation
			double xVariance = 0;
			double yVariance = 0;
			double headingVariance = 0;
			Double x = null;
			Double y = null;
			Double heading = null;
			for (Pose pose : entry.getValue())
			{
				if (x == null)
				{
					x = pose.getX();
					y = pose.getY();
					heading = pose.getHeading();
				}
				xVariance = Math.abs(x - pose.getX());
				yVariance = Math.abs(y - pose.getY());
				headingVariance = Math.abs(heading - pose.getHeading());
			}

			// check if the scan's pose is useable
			if (xVariance < 5 && yVariance < 5 && headingVariance < 2)
			{
				listener.useScan(entry.getKey(), entry.getValue().iterator().next());

				removeMatchedScanFromParticles(particles, entry);
			}

		}
	}

	private void removeMatchedScanFromParticles(List<Particle> particles,
			Map.Entry<ParticleFilterObservationSet, Set<Pose>> entry)
	{
		// remove this scan for all of the particles that reference it
		for (Particle particle : particles)
		{
			Iterator<ScanReference> itr = particle.getScanReferences().iterator();
			while (itr.hasNext())
			{
				ScanReference scanReference = itr.next();
				if (scanReference.getScan().equals(entry.getKey()))
				{
					itr.remove();
				}
			}
		}
	}

	private Set<Pose> getScanReference(ParticleFilterObservationSet scan,
			Map<ParticleFilterObservationSet, Set<Pose>> scansReferences)
	{
		Set<Pose> poses = scansReferences.get(scan);
		if (poses == null)
		{
			poses = new HashSet<>();
			scansReferences.put(scan, poses);
		}
		return poses;
	}
}
